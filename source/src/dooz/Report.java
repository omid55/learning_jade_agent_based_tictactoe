/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dooz;

import java.util.Vector;

/**
 *
 * @author SONY
 */
public class Report
{
    public Vector<Integer> win;
    public boolean wasItFirst;

    public Report()
    {
    }

    public Report(Vector<Integer> win, boolean wasItFirst)
    {
        this.win=new Vector<Integer>();
        // copy whole vector to this vector
        for(int i=0;i<win.size();i++)
        {
            this.win.add(win.elementAt(i));
        }
        this.wasItFirst = wasItFirst;
    }

    @Override
    public String toString()
    {
        String res="";
        for(int i=0;i<win.size();i++)
        {
            res+=win.elementAt(i)+",";
        }
        res+=wasItFirst;
        return res;
    }
    
}
