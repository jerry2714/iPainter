package ledcubeproject.painter20;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import ledcubeproject.models.ledcube.LedCubeDataModel;
import ledcubeproject.models.ledcubecontroller.BluetoothLedCubeController;
import ledcubeproject.models.ledcubecontroller.LedCubeController;
import ledcubeproject.ui.SimpleDialog;

import static android.widget.Toast.makeText;


public class PainterActivity extends AppCompatActivity implements
        View.OnClickListener{

    LedCubeDataModel ledCubeDataModel = new LedCubeDataModel(6);
    BluetoothLedCubeController bluetoothLedCubeController = new BluetoothLedCubeController();
    pathRecorder pathRecorder = new pathRecorder();

    final int alphaMask = 0xFF000000;

    boolean buttonPressed;
    int Color;
    int function = 0;
    int level = 0;
    Context context;

    ImageButton paint, clear, clearall, undo, redo;
    TextView txvLevel;
    Button color_picker;
    ColorPickerDialog dialog;


    PairedListDialog pairedListDialog;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActBarDrawerToggle;

    TextView t = null;
    int tv[] = {
            R.id.tv_110, R.id.tv_210, R.id.tv_310, R.id.tv_410, R.id.tv_510, R.id.tv_610,
            R.id.tv_120, R.id.tv_220, R.id.tv_320, R.id.tv_420, R.id.tv_520, R.id.tv_620,
            R.id.tv_130, R.id.tv_230, R.id.tv_330, R.id.tv_430, R.id.tv_530, R.id.tv_630,
            R.id.tv_140, R.id.tv_240, R.id.tv_340, R.id.tv_440, R.id.tv_540, R.id.tv_640,
            R.id.tv_150, R.id.tv_250, R.id.tv_350, R.id.tv_450, R.id.tv_550, R.id.tv_650,
            R.id.tv_160, R.id.tv_260, R.id.tv_360, R.id.tv_460, R.id.tv_560, R.id.tv_660
    };

    Runnable sendPattern = new Runnable(){
        @Override
        public void run() {
            ledCubeDataModel.regenerateCubeData();
            int datas[] = ledCubeDataModel.getOutputBuffer();
            try {
                byte[] buf = bluetoothLedCubeController.command(LedCubeController.ASSIGN, LedCubeController.SET+LedCubeController.DISPLAY, datas.length);
                bluetoothLedCubeController.addToQueue(buf);
                for(int data : datas)
                {
                    bluetoothLedCubeController.addToQueue(data);
                    System.out.println(Integer.toHexString(data));
                }
                bluetoothLedCubeController.sendQueue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level01);
        initViews();

        txvLevel = (TextView) findViewById(R.id.txvLevel);
        paint = (ImageButton) findViewById(R.id.paint);
        paint.setOnClickListener(this);
        clear = (ImageButton) findViewById(R.id.clear);
        clear.setOnClickListener(this);
        clearall = (ImageButton) findViewById(R.id.clearall);
        clearall.setOnClickListener(this);
        undo = (ImageButton) findViewById(R.id.imbundo);
        redo = (ImageButton) findViewById(R.id.imbredo);

        color_picker = (Button) findViewById(R.id.color_picker);

        txvLevel.setText(getResources().getStringArray(R.array.levels)[0]);
        pairedListDialog = new PairedListDialog();

        paint.setImageDrawable(getResources().getDrawable(R.drawable.ic_grease_pencil_grey600_36dp));

        // 設定側開式選單。
        ActionBar actBar = getSupportActionBar();
        actBar.setDisplayHomeAsUpEnabled(true);
        actBar.setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mActBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.app_name, R.string.app_name);
        mActBarDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mActBarDrawerToggle);

        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<CharSequence> arrAdapWeekday =
                ArrayAdapter.createFromResource(this, R.array.levels,
                        android.R.layout.simple_list_item_1);
        listView.setAdapter(arrAdapWeekday);
        listView.setOnItemClickListener(listViewOnItemClick);

        for (int i = 0; i < 36; ++i)
            if ((t = (TextView) findViewById(tv[i])) != null) {
                t.setOnClickListener(this);
            }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(bluetoothLedCubeController.isConnected())
            bluetoothLedCubeController.disconnect();
    }

    @Override
    public void onClick(View view) {

        if(paint.isPressed())
        {
            function=0;
            paint.setImageDrawable(getResources().getDrawable(R.drawable.ic_grease_pencil_grey600_36dp));
            clear.setImageDrawable(getResources().getDrawable(R.drawable.ic_eraser_black_36dp));
        }
        if(clear.isPressed())
        {
            function=1;
            paint.setImageDrawable(getResources().getDrawable(R.drawable.ic_grease_pencil_black_36dp));
            clear.setImageDrawable(getResources().getDrawable(R.drawable.ic_eraser_grey600_36dp));
        }


        for(int i = 0; i < 36; ++i)
        {
            redo.setEnabled(false);
            if(view.getId()==findViewById(tv[i]).getId())
            {
                t = (TextView) findViewById(tv[i]);
                final GradientDrawable g=(GradientDrawable)t.getBackground();
                switch(function) {
                    case 0:
                        pathRecorder.record(i, level, ledCubeDataModel.getColor(i % 6, i / 6, level), Color);
                        g.setColor(Color);
                        ledCubeDataModel.setColor(i % 6, i / 6, level, Color);
                        break;
                    case 1:
                        pathRecorder.record(i, level, ledCubeDataModel.getColor(i % 6, i / 6, level), alphaMask);
                        g.setColor(alphaMask);
                        ledCubeDataModel.setColor(i % 6, i / 6, level, 0);
                        break;
                }

            }
        }

        switch (view.getId()){
            case R.id.clearall:
                int pos[] = new int[tv.length*6];
                int level[] = new int[tv.length*6];
                int preColor[] = new int[tv.length*6];
                int newColor[] = new int[tv.length*6];
                for (int i = 0; i < tv.length; ++i)
                    if ((t = (TextView) findViewById(tv[i])) != null) {

                        final GradientDrawable gradientDrawable=(GradientDrawable)t.getBackground();
                        gradientDrawable.setColor(alphaMask);
                    }
                for(int l = 0; l < 6; l++)
                    for(int i = 0; i < tv.length; i++)
                    {
                        pos[l*tv.length + i] = i;
                        level[l*tv.length + i] = l;
                        preColor[l*tv.length + i] = ledCubeDataModel.getColor(i % 6, i / 6, l);
                        newColor[l*tv.length + i] = alphaMask;
                    }
                pathRecorder.record(pos, level, preColor, newColor);
                ledCubeDataModel.clear();
                redo.setEnabled(false);
                break;
        }

    }

    /**
     * 送出按鈕: 未連線時執行連線功能，已連線時執行送出功能
     * @param v
     */
    public void sendOut(View v)
    {
        if(bluetoothLedCubeController.isDeviceSupportBluetooth())
        {
            if(!bluetoothLedCubeController.isOpen())
            {
                bluetoothLedCubeController.open(this);
                return;
            }
            if(!bluetoothLedCubeController.isConnected())
            {
                pairedListDialog.update(bluetoothLedCubeController.listBoundedDevicesName());
                pairedListDialog.show();
            }
            else{
                new Thread(sendPattern).start();
            }
        }
        else {
            makeText(this, "此裝置不支援藍牙", Toast.LENGTH_SHORT);
            return;
        }

    }

    public void recordPattern(View view)
    {

        final SimpleDialog saveDialog = new SimpleDialog(this, R.layout.record_file, "存檔");
        final EditText edtFileName = (EditText) saveDialog.getView().findViewById(R.id.fileName);
        Button confirm = (Button) saveDialog.getView().findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = edtFileName.getText().toString();
                if(fileName.equals(""))
                {
                    Calendar cl= Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                    fileName = sdf.format(cl.getTime());
                }
                saveDialog.close();
                recordPattern(fileName);

            }
        });

        saveDialog.show();

    }

    public void undo(View v)
    {
        Log.d("123456", "undo");
        Record rs[] = pathRecorder.undo();
        if(rs == null)
        {
            Log.d("null", "nothing to undo");
            return;
        }
        redo.setEnabled(true);
        for(Record r : rs)
        {
            if(r.getLevel() == level)
            {
                TextView t = (TextView) findViewById(tv[r.getPos()]);
                GradientDrawable g = (GradientDrawable) t.getBackground();
                g.setColor(alphaMask | r.getPreviousColor());
            }
            ledCubeDataModel.setColor(r.getPos()%6, r.getPos()/6, r.getLevel(), r.getPreviousColor());
        }

    }

    public void redo(View v)
    {
        Log.d("123456", "redo");
        Record rs[] = pathRecorder.redo();
        if(rs == null)
        {
            Log.d("null", "nothing to redo");
            return;
        }
        for(Record r : rs)
        {
            if(r.getLevel() == level)
            {
                TextView t = (TextView) findViewById(tv[r.getPos()]);
                GradientDrawable g = (GradientDrawable) t.getBackground();
                g.setColor(alphaMask | r.getLaterColor());
            }
            /*else
            {
                Log.d("level check", "r.getLevel: " + r.getLevel() + "  level: " + level);
            }*/
            ledCubeDataModel.setColor(r.getPos()%6, r.getPos()/6, r.getLevel(),r.getLaterColor());
        }
    }


    public void recordPattern(String fileName)
    {
        ledCubeDataModel.regenerateCubeData();
        int[] datas = ledCubeDataModel.getOutputBuffer();

        Hashtable<Integer, ArrayList<Integer>> patternDescription = new Hashtable<>();
        ArrayList<Integer> list = null;
        for(int data : datas)
        {
            if((data & 0x00FFFFFF) == 0)
                continue;
            list = patternDescription.get((data & 0x00FFFFFF));
            if(list == null)
            {
                list = new ArrayList<>();
                patternDescription.put((data & 0x00FFFFFF), list);
            }
            list.add((data >> 24) & 0x000000FF);
        }


        Enumeration<Integer> keys = patternDescription.keys();


        FileWriter fw;
        BufferedWriter bw;
        try {
            String path = Environment.getExternalStorageDirectory().getPath()+"/painter2.0_save/";
            File dir = new File(path);
            if(!dir.exists())
                dir.mkdir();
            fw = new FileWriter(path+fileName+".txt", false);
            bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結

            String colorStr = "const uint32_t color[size_color] = {";
            int count = 0;
            while(keys.hasMoreElements())
            {
                count++;
                int k = keys.nextElement();
                list = patternDescription.get(k);
                bw.write("const uint8_t pos_" + k + "[" + (list.size()+1) + "] = {" + list.size());
                for(int i = 0; i < list.size(); i++)
                {
                    int v = list.get(i);
                    bw.write("," + v);
                }
                bw.write("};");
                bw.newLine();

                colorStr = colorStr + k;
                if(keys.hasMoreElements())
                    colorStr += ", ";
                else
                    colorStr += "};";
            }
            bw.write("static const uint8_t size_color = " + count + ";");
            bw.newLine();
            bw.write(colorStr);
            bw.newLine();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        color_picker = (Button) findViewById(R.id.color_picker);
        color_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialog == null)
                    dialog = new ColorPickerDialog(context, color_picker.getTextColors().getDefaultColor(),
                        getResources().getString(R.string.btn),
                        new ColorPickerDialog.OnColorChangedListener() {
                            @Override
                            public void colorChanged(int color) {
                                Drawable d = color_picker.getBackground();
                                d.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                                Color = color;
                                color_picker.setBackground(d);
                                //color_picker.setBackgroundColor(color);
                            }
                        });
                dialog.show();
            }
        });

        for(int i = 0; i < tv.length; i++)
        {
            TextView t = (TextView) findViewById(tv[i]);
            //t.setText(""+i);
            //t.setTextColor(0xFFFFFFFF);
            GradientDrawable g=(GradientDrawable)t.getBackground();
            g.setColor(0xFF000000);
        }

    }
    //-------------------------------------------------------------------------------------------------------------
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActBarDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 要先把選單的項目傳給 ActionBarDrawerToggle 處理。
        // 如果它回傳 true，表示處理完成，不需要再繼續往下處理。
        if (mActBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AdapterView.OnItemClickListener listViewOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            level = position;
            txvLevel.setText(getResources().getStringArray(R.array.levels)[position]);
            TextView t;
            for(int i = 0; i < tv.length; i++)
            {
                t = (TextView) findViewById(tv[i]);
                final GradientDrawable gradientDrawable = (GradientDrawable) t.getBackground();
                gradientDrawable.setColor(0xFF000000 | ledCubeDataModel.getColor(i%6, i/6, level));
            }
            mDrawerLayout.closeDrawers();
        }
    };



    private class PairedListDialog
    {
        View dialogView;
        AlertDialog dialog;
        ListView pairedLv;
        PairedListAdapter pairedListAdapter = new PairedListAdapter();
        PairedListListener pairedListListener = new PairedListListener();

        public PairedListDialog()
        {
           // dialogView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.paired_list, null);
            pairedLv = new ListView(getApplication());
            pairedLv.setAdapter(pairedListAdapter);
            pairedLv.setOnItemClickListener(pairedListListener);

            dialog = (new AlertDialog.Builder(PainterActivity.this)).setTitle("以配對裝置列表").setView(pairedLv).create();
        }

        public void update(String[] strarr)
        {
            pairedListAdapter.setPairedDeviceNames(strarr);
            pairedListAdapter.notifyDataSetChanged();
        }

        public void show()
        {
            dialog.show();
        }
        public void close()
        {
            dialog.dismiss();
        }
    }

    private class PairedListAdapter extends BaseAdapter
    {
        String[] pairedDeviceNames = null;

        public void setPairedDeviceNames(String[] names)
        {
            pairedDeviceNames = names;
        }

        @Override
        public int getCount() {
            if(pairedDeviceNames != null)
                return pairedDeviceNames.length;
            else return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            if(v == null){      //如果沒有前一個版本則新增
                v = new TextView(getApplicationContext());
                ((TextView)v).setText(pairedDeviceNames[i]);
                ((TextView)v).setTextSize(20);
                v.setTag(pairedDeviceNames[i]);
            }
            return v;
        }
    }

    private class PairedListListener implements AdapterView.OnItemClickListener{
        boolean connected = false;
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            connected = false;
            String str = (String) view.getTag();
            //Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            //addLog("與" + str + "連線");
            bluetoothLedCubeController.clean();
            bluetoothLedCubeController.establishSocket(str);

            pairedListDialog.close();
            Thread thread = new Thread(){
                public void run()
                {
                    if(!bluetoothLedCubeController.connect())
                    {
                        connected = false;
                        PainterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "連線失敗", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        //Toast.makeText(getApplicationContext(), "連線成功", Toast.LENGTH_SHORT).show();
                        connected = true;
                        PainterActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "連線成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                        new Thread(sendPattern).start();
                    }
                }
            };
            thread.start();

        }
    }

}


