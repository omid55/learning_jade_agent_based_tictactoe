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
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author SONY
 */
public class MainAgent extends Agent
{
    private MainFrame frame;
    public AID opponentAid;

    @Override
    protected void setup()
    {
        frame=new MainFrame(this);
        frame.show();

        ServiceDescription sd=new ServiceDescription();
        sd.setType("Main");
        sd.setName("mainAgent");
        DFAgentDescription dfd=new DFAgentDescription();
        dfd.setName(getAID());
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);
        }
        catch (FIPAException ex)
        {
            JOptionPane.showMessageDialog(null,"An Error Occurred In Registering MainAgent Into The DFService\n\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        addBehaviour(new takeOpponentSelection());
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
            JOptionPane.showMessageDialog(null,"An Error Occurred In Deregistering MainAgent From The DFService\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
        super.takeDown();
        System.out.println("Now "+getName()+" Terminated Successfully .");
    }

    public void createOpponentAgent()
    {
        String agentName="myOpponent";
        PlatformController pc=getContainerController();
        try
        {
            pc.createNewAgent(agentName, OpponentAgent.class.getName(), null).start();
            String mainName=getName();
            int index=mainName.indexOf('@');
            String tail=mainName.substring(index);
            agentName+=tail;
            opponentAid=new AID(agentName, AID.ISGUID);
        }
        catch (ControllerException ex)
        {
            JOptionPane.showMessageDialog(frame, "An Error While Creating Opponent Agent Occured\n\n"+ex.getLocalizedMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
        }
    }

    public void doIncrementNumberOfPlays()
    {
        int num=Integer.parseInt(frame.NumberOfPlayLbl.getText());
        frame.NumberOfPlayLbl.setText(String.valueOf(num+1));
    }

    public void sendMainAgPlay(int location)
    {
        int winner=frame.whoWins();
        if(winner!=0)     // it means somebody wins
        {
            if(winner==1)
            {
                JOptionPane.showMessageDialog(frame, "MainAgent (You) Won !!!","Win",JOptionPane.INFORMATION_MESSAGE);
                frame.mainAgWins++;
                frame.clearAndUpdateWinLbls();
            }
            else
            {
                JOptionPane.showMessageDialog(frame, "OpponentAgent Won !!!","Win",JOptionPane.INFORMATION_MESSAGE);
                frame.OpponentAgWins++;
                frame.clearAndUpdateWinLbls();
            }
            ACLMessage msg=new ACLMessage(ACLMessage.CFP);
            msg.setContent(location+","+winner);
            msg.addReceiver(opponentAid);
            this.send(msg);
            doIncrementNumberOfPlays();
        }
        else if(frame.doCheckTie())
        {
            JOptionPane.showMessageDialog(frame, "Tie , No One Won !!!","Tie",JOptionPane.INFORMATION_MESSAGE);
            frame.clearAndUpdateWinLbls();
            ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
            msg.setContent(location+","+winner);
            msg.addReceiver(opponentAid);
            this.send(msg);
            doIncrementNumberOfPlays();
        }
        else
        {
            ACLMessage msg=new ACLMessage(ACLMessage.PROPOSE);
            msg.setContent(String.valueOf(location));
            msg.addReceiver(opponentAid);
            this.send(msg);
        }
    }

    private class takeOpponentSelection extends CyclicBehaviour
    {

        @Override
        public void action()
        {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg=myAgent.receive(mt);
            if(msg!=null)
            {
                int selected=Integer.parseInt(msg.getContent());
                frame.doSelectOpponent(selected);

                int winner=frame.whoWins();
                if(winner!=0)     // it means somebody wins
                {
                    if(winner==1)
                    {
                        JOptionPane.showMessageDialog(frame, "MainAgent (You) Won !!!","Win",JOptionPane.INFORMATION_MESSAGE);
                        frame.mainAgWins++;
                        frame.clearAndUpdateWinLbls();
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(frame, "OpponentAgent Won !!!","Win",JOptionPane.INFORMATION_MESSAGE);
                        frame.OpponentAgWins++;
                        frame.clearAndUpdateWinLbls();
                    }
                    ACLMessage message=new ACLMessage(ACLMessage.CFP);
                    message.setContent("10"+","+winner);   // 10 for location it means that Opponent itself did this last selection and it doesn't want to add this again to its vector
                    message.addReceiver(opponentAid);
                    myAgent.send(message);
                    doIncrementNumberOfPlays();
                }
                else if(frame.doCheckTie())
                {
                    JOptionPane.showMessageDialog(frame, "Tie , No One Won !!!","Tie",JOptionPane.INFORMATION_MESSAGE);
                    frame.clearAndUpdateWinLbls();
                    ACLMessage message=new ACLMessage(ACLMessage.INFORM);
                    message.setContent("10"+","+winner);
                    message.addReceiver(opponentAid);
                    myAgent.send(message);
                    doIncrementNumberOfPlays();
                }

            }
            else
            {
                block();
            }
        }

    }
}
