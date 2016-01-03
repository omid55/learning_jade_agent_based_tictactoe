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
public class WayItem
{
    public Vector<Integer> way;
    public double good;

    public WayItem()
    {
    }

    public WayItem(Vector<Integer> way, double good)
    {
        this.way=new Vector<Integer>();
        // copy whole vector to this vector
        for(int i=0;i<way.size();i++)
        {
            this.way.add(way.elementAt(i));
        }
        this.good = good;
    }
    
}
