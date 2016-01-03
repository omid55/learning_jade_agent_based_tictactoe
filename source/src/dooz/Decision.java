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
public class Decision
{
    public Vector<Integer> way;
    public int index;

    public Decision()
    {
        index=0;
        way=new Vector<Integer>();
    }

    public Decision(Vector<Integer> way)
    {
        this.way=new Vector<Integer>();
        // copy whole vector to this vector
        for(int i=0;i<way.size();i++)
        {
            this.way.add(way.elementAt(i));
        }
        this.index = 0;
    }

    public void setWay(Vector<Integer> way)
    {
        index=0;
        this.way=new Vector<Integer>();
        // copy whole vector to this vector
        for(int i=0;i<way.size();i++)
        {
            this.way.add(way.elementAt(i));
        }
    }

    public void clear()
    {
        way.clear();
        index=0;
    }
}
