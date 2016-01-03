/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dooz;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Vector;
import javax.swing.JOptionPane;
import sun.misc.Compare;
import sun.misc.Sort;

/**
 *
 * @author SONY
 */
public class OpponentAgent extends Agent
{
    private OpponentFrame frame;
    private Vector<Report> wins;
    private Vector<Integer> nowMainPlay;
    private Vector<Integer> nowMyPlay;    // Opponent Play
    private Turn myTurn;
    private Decision nowDecision;
    private AID mainAg;

    @Override
    protected void setup()
    {
        frame=new OpponentFrame(this);
        frame.show();
        wins=new Vector<Report>();
        nowMainPlay=new Vector<Integer>();
        nowMyPlay=new Vector<Integer>();
        myTurn=Turn.IAmSecond;
        nowDecision=new Decision();

        ServiceDescription sd=new ServiceDescription();
        sd.setType("Opponent");
        sd.setName("opponentAgent");
        DFAgentDescription dfd=new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);
        }
        catch (FIPAException ex)
        {
            JOptionPane.showMessageDialog(null,"An Error Occurred In Registering OpponentAgent Into The DFService\n\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }

        addBehaviour(new takeMainPlayAndDoPlay());
        addBehaviour(new takeWinPlay());
        addBehaviour(new takeTiePlay());
    }

    @Override
    protected void takeDown()
    {
        try
        {
            DFService.deregister(this);
        }
        catch (FIPAException ex)
        {
            JOptionPane.showMessageDialog(null,"An Error Occurred In Deregistering OpponentAgent Into The DFService\n\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        super.takeDown();
        System.out.println("Now "+getName()+" Terminated Successfully .");
    }

    public Vector<Report> getWins()
    {
        return wins;
    }
    
    public int getRandomSelect()     // works with nowPlay Vector
    {
        Vector<Integer> tmp=new Vector<Integer>();
        // initialize tmp
        for(int i=0;i<9;i++)
        {
            if(nowMainPlay.contains(i)==false && nowMyPlay.contains(i)==false)
            {
                tmp.add(i);
            }
        }
        
        Random r=new Random();
        int index=r.nextInt(tmp.size());
        return tmp.elementAt(index);
    }

    public void doPlay(AID mainAid,int select)
    {
        nowMyPlay.add(select);
        ACLMessage msg=new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
        msg.setContent(String.valueOf(select));
        msg.addReceiver(mainAid);
        this.send(msg);
    }

    public void goForNextPlay()
    {
        // go for next play
        nowMyPlay.clear();
        nowMainPlay.clear();
        nowDecision.clear();
        myTurn=(myTurn==Turn.IAmFirst) ? Turn.IAmSecond : Turn.IAmFirst;
        if(myTurn==Turn.IAmFirst)
        {
            // it means I(opponent agent) must start the game
            Vector<Integer> indexes=new Vector<Integer>();
            for(int i=0;i<wins.size();i++)
            {
                if(wins.elementAt(i).wasItFirst)
                {
                    indexes.add(i);
                }
            }
            if(indexes.size()==0)   // we had no first winner up to now
            {
                int selection=getRandomSelect();
                frame.addLog("By Random Selected ...\n");
                doPlay(mainAg, selection);
            }
            else
            {
                Random rand=new Random();
                int ind=rand.nextInt(indexes.size());
                int index=indexes.elementAt(ind);
                nowDecision.setWay(wins.elementAt(index).win);
                int selection=nextSelectionOfMyDecision();
                frame.addLog("NowDecision =>  "+nowDecision.way.toString()+"\n");
                doPlay(mainAg, selection);
            }
        }
    }

    public int indexOfInArray(int[] a,int value)
    {
        for(int i=0;i<a.length;i++)
        {
            if(a[i]==value) return i;
        }
        return -1;    // it means error
    }

    private final int RIGHT=0;
    private final int DOWN=1;
    private final int LEFT=2;

    public int getLocInAnotherDimension(int loc,int dim)
    {
        int[] right=new int[]{2,5,8,1,4,7,0,3,6};
        int[] down=new int[]{8,7,6,5,4,3,2,1,0};
        int[] left=new int[]{6,3,0,7,4,1,8,5,2};
        switch(dim)
        {
            case RIGHT:
                return indexOfInArray(right, loc);

            case DOWN:
                return indexOfInArray(down, loc);

            case LEFT:
                return indexOfInArray(left, loc);
        }
        return -1;     // it means error
    }

    public void addDistinct2Wins(Vector<Integer> v,boolean b)
    {
        for(int i=0;i<wins.size();i++)
        {
            Report tmp=wins.elementAt(i);
            if(tmp.win.size()==v.size())
            {
                boolean eq=true;
                for(int j=0;j<tmp.win.size();j++)
                {
                    if(tmp.win.elementAt(j)!=v.elementAt(j))
                    {
                        eq=false;
                        break;
                    }
                }
                if(eq)
                {
                    return;
                }
            }
        }
        wins.add(new Report(v, b));
        frame.NumberOfLearnsLabel.setText(String.valueOf(wins.size()));
    }

    public void add2Wins(int winner)   // 1 means MainAgent  and  2 means OpponentAgent
    {
        if(winner==1)    // MainAgent Won
        {
            boolean u=(myTurn!=Turn.IAmFirst);
            addDistinct2Wins(nowMainPlay, u);
            Vector<Integer> tmp=new Vector<Integer>();
            for(int i=0;i<nowMainPlay.size();i++)
            {
                tmp.add(getLocInAnotherDimension(nowMainPlay.elementAt(i),LEFT));
            }
            addDistinct2Wins(tmp, u);

            tmp.clear();
            for(int i=0;i<nowMainPlay.size();i++)
            {
                tmp.add(getLocInAnotherDimension(nowMainPlay.elementAt(i),DOWN));
            }
            addDistinct2Wins(tmp, u);

            tmp.clear();
            for(int i=0;i<nowMainPlay.size();i++)
            {
                tmp.add(getLocInAnotherDimension(nowMainPlay.elementAt(i),RIGHT));
            }
            addDistinct2Wins(tmp, u);
        }
        else if(winner==2)   // OpponentAgent Won
        {
            boolean u=(myTurn==Turn.IAmFirst);
            addDistinct2Wins(nowMyPlay, u);
            Vector<Integer> tmp=new Vector<Integer>();
            for(int i=0;i<nowMyPlay.size();i++)
            {
                tmp.add(getLocInAnotherDimension(nowMyPlay.elementAt(i),LEFT));
            }
            addDistinct2Wins(tmp, u);

            tmp.clear();
            for(int i=0;i<nowMyPlay.size();i++)
            {
                tmp.add(getLocInAnotherDimension(nowMyPlay.elementAt(i),DOWN));
            }
            addDistinct2Wins(tmp, u);

            tmp.clear();
            for(int i=0;i<nowMyPlay.size();i++)
            {
                tmp.add(getLocInAnotherDimension(nowMyPlay.elementAt(i),RIGHT));
            }
            addDistinct2Wins(tmp, u);
        }
        else    // Error Occurred
        {
            JOptionPane.showMessageDialog(frame, "An Error Occured In Parsing Winner In Add2Wins Function ...","ERROR",JOptionPane.ERROR_MESSAGE);
            return ;
        }
    }

    // this functoin checks that all of locations of a way are empty or filled by opponent symbol
    // and if there is another symbol (symbol of MainAgent) in this way return -1 that it means this way is not good
    // if there is only empty and my symbol this function return number of my symbol divided by number of locations of this way
    public double checkThisWay(Vector<Integer> way)
    {
        int r=0;
        for(int i=0;i<way.size();i++)
        {
            if(nowMainPlay.contains(way.elementAt(i)))
            {
                return -1;    // it means MainAgent selected this location before so this way is not good
            }
            else    // so this is empty or filled by my symbol (opponent symbol)
            {
                if(nowMyPlay.contains(way.elementAt(i)))
                {
                    r++;   // it means filled by my symbol (opponent symbol)
                }
            }
        }
        double res=(double)r/(double)way.size();
        return res;
    }

    public double checkThisWayForUserMoves(Vector<Integer> way)
    {
        int r=0;
        for(int i=0;i<way.size();i++)
        {
            if(nowMyPlay.contains(way.elementAt(i)))
            {
                return -1;    // it means MainAgent selected this location before so this way is not good
            }
            else    // so this is empty or filled by my symbol (opponent symbol)
            {
                if(nowMainPlay.contains(way.elementAt(i)))
                {
                    r++;   // it means filled by my symbol (opponent symbol)
                }
            }
        }
        double res=(double)r/(double)way.size();
        return res;
    }

    public int nextSelectionOfMyDecision()
    {
        try
        {
            int r=-1;
            do
            {
                r=nowDecision.way.elementAt(nowDecision.index++);
            }
            while(nowMainPlay.contains(r)==true || nowMyPlay.contains(r)==true);
            return r;
        }
        catch(Exception ex)
        {
            //JOptionPane.showMessageDialog(frame, "An Error Occured In nextSelectionOfMyDecision Function \n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
            //return -1;
            frame.addLog("By Random Selected In nextSelectionOfMyDecision After Exception ...\n");
            return getRandomSelect();
        }
    }

    private class takeWinPlay extends CyclicBehaviour
    {

        @Override
        public void action() 
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                String content=msg.getContent();
                int l=0;
                while(content.charAt(l)!=',')
                {
                    l++;
                }
                int loc=-1;
                int winner=-1;
                try
                {
                    loc=Integer.parseInt(content.subSequence(0, l).toString());
                    winner=Integer.parseInt(content.subSequence(l+1, content.length()).toString());
                }
                catch(Exception ex)
                {
                    JOptionPane.showMessageDialog(frame, "An Error Occured In Parsing Message From "+msg.getSender()+"\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
                    return ;
                }
                if(loc!=10)    // 10 for location it means that Opponent itself did this last selection and it doesn't want to add this again to its vector
                {
                    nowMainPlay.add(loc);
                }
                add2Wins(winner);
                goForNextPlay();
            }
            else
            {
                block();
            }
        }
        
    }

    private class takeTiePlay extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                // i throw away tie plays
                // if want to use tie plays check here and use nowMyPlay and nowMainPlay and save them and learn them by that mean ;)
                goForNextPlay();
            }
            else
            {
                block();
            }
        }

    }

    private class takeMainPlayAndDoPlay extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                if(mainAg==null) mainAg=msg.getSender();
                int selected=Integer.parseInt(msg.getContent());
                frame.addLog("A Message Has Been Received From MainFrame That He/She Selected "+selected+" Now .");
                nowMainPlay.add(selected);
                if(wins.size()==0)    // if wins is empty
                {
                    int mySelection=getRandomSelect();
                    frame.addLog("By Random Selected ...\n");
                    doPlay(msg.getSender(),mySelection);
                }
                else    // it means agent learned something up to now
                {
                    // Now Decision Time With Algorithm
                    /*
                    if(nowDecision.way.size()!=0)
                    {
                        double isItGoodYet=checkThisWay(nowDecision.way);
                        if(isItGoodYet!=-1)
                        {
                            // it means it is good yet and i must go on this way
                            doPlay(msg.getSender(),nextSelectionOfMyDecision());
                            return ;
                        }
                    }
                    */


                    // it means nowDecision is empty or it means nowDecision.way is not good any more and this way identified by user
                    // now i should choose the best way of wins that it can be usefull in now play
                    Vector<WayItem> usefull=new Vector<WayItem>();
                    Vector<WayItem> defence=new Vector<WayItem>();
                    for(int i=0;i<wins.size();i++)
                    {
                        double r=checkThisWay(wins.elementAt(i).win);
                        if(r!=-1)
                        {
                            usefull.add(new WayItem(wins.elementAt(i).win, r));
                        }

                        double rd=checkThisWayForUserMoves(wins.elementAt(i).win);
                        if(rd!=-1)
                        {
                            defence.add(new WayItem(wins.elementAt(i).win,rd));
                        }
                    }
                    if(usefull.size()==0 && defence.size()==0)
                    {
                        int mySelection=getRandomSelect();
                        frame.addLog("By Random Selected ...\n");
                        doPlay(msg.getSender(),mySelection);
                        return ;
                    }
                    else
                    {
                        Compare cmpr=new Compare()
                        {
                            public int doCompare(Object o1, Object o2)
                            {
                                if((((WayItem)o1).good==((WayItem)o2).good)) return 0;
                                else if(((WayItem)o1).good<((WayItem)o2).good) return 1;
                                else return -1;
                            }
                        };
                        Object[] usefullArray=usefull.toArray();
                        Sort.quicksort(usefullArray, cmpr);
                        Object[] defenceArray=defence.toArray();
                        Sort.quicksort(defenceArray, cmpr);
                        Vector<Integer> maxes=new Vector<Integer>();
                        if(usefullArray.length==0 || (defenceArray.length!=0 && ((WayItem)defenceArray[0]).good > ((WayItem)usefullArray[0]).good))
                        {
                            // get defence role
                            int index=1;
                            maxes.add(0);
                            WayItem max=(WayItem)defenceArray[0];
                            while(index<defenceArray.length)
                            {
                                WayItem wi=(WayItem)defenceArray[index];
                                if(wi.good==max.good)
                                {
                                    maxes.add(index);
                                }
                                else
                                {
                                    break;
                                }
                                index++;
                            }
                            // now select between all of good ways for defencing
                            Random rand=new Random();
                            int ind=rand.nextInt(maxes.size());
                            nowDecision.setWay(((WayItem)defenceArray[maxes.elementAt(ind)]).way);
                            frame.addLog("NowDecision With Defence Role =>  "+nowDecision.way.toString()+"\n");
                            doPlay(msg.getSender(),nextSelectionOfMyDecision());
                        }
                        else
                        {
                            // get offence role
                            int index=1;
                            maxes.add(0);
                            WayItem max=(WayItem)usefullArray[0];
                            while(index<usefullArray.length)
                            {
                                WayItem wi=(WayItem)usefullArray[index];
                                if(wi.good==max.good)
                                {
                                    maxes.add(index);
                                }
                                else
                                {
                                    break;
                                }
                                index++;
                            }
                            // now select between all of good ways
                            Random rand=new Random();
                            int ind=rand.nextInt(maxes.size());
                            nowDecision.setWay(((WayItem)usefullArray[maxes.elementAt(ind)]).way);
                            frame.addLog("NowDecision =>  "+nowDecision.way.toString()+"\n");
                            doPlay(msg.getSender(),nextSelectionOfMyDecision());
                        }
                    }
                }
            }
            else
            {
                block();
            }
        }

    }

}
