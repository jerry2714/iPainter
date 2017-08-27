package ledcubeproject.painter20;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

/**
 * Created by Jerry on 2017/4/14.
 */

public class pathRecorder {
    Deque<Record[]> undo, redo;

    public pathRecorder()
    {
        undo = new ArrayDeque<>();
        redo = new ArrayDeque<>();
    }
    public Record[] undo()
    {
        Record r[] = null;
        try {
            r = undo.removeFirst();
            if(r != null)
                redo.addFirst(r);
        }catch (Exception e){}
        return r;
    }
    public Record[] redo()
    {
        Record r[] = null;
        try {
            r = redo.removeFirst();
            if(r != null)
                undo.addFirst(r);
        }catch (Exception e){}
        return r;
    }

    public void record(int pos, int level, int preColor, int newColor)
    {
        Record r[] = new Record[1];
        r[0] = new Record(pos, level, preColor, newColor);
        try {
            undo.addFirst(r);
            redo.clear();
            if(undo.size() >= 200)
                undo.removeLast();
        }catch (Exception e){}
    }

    public void record(int[] pos, int[] level, int[] preColor, int[] newColor)  throws ArrayIndexOutOfBoundsException
    {
        Record r[] = new Record[pos.length];

        for(int i = 0; i < r.length; i++)
            r[i] = new Record(pos[i], level[i], preColor[i], newColor[i]);
        try{
            undo.addFirst(r);
            redo.clear();
            if(undo.size() >= 200)
                undo.removeLast();
        }catch (Exception e){}
    }


}
class Record{
    private int pos;
    private int level;
    private int previousColor;
    private int laterColor;

    Record(int pos, int level, int previousColor, int newColor)
    {
        this.pos = pos;
        this.level = level;
        this.laterColor = newColor;
        this.previousColor = previousColor;
    }

    public int getPos(){return pos;}

    public int getLevel(){return level;}
    public int getLaterColor() {
        return laterColor;
    }

    public int getPreviousColor() {
        return previousColor;
    }
}
