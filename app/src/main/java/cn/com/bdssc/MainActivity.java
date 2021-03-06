package cn.com.bdssc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.SDKInitializer;

import java.util.ArrayList;
import java.util.List;

import cn.com.bdssc.adapter.KaijiangAdapter;
import cn.com.bdssc.adapter.ShapeLoadingDialog;
import cn.com.bdssc.bean.KaiJiangInfo;
import cn.com.bdssc.util.CheckUtil;
import cn.com.bdssc.util.ParseJsonUtil;
import cn.jpush.android.api.JPushInterface;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ShapeLoadingDialog shapeLoadingDialog;
    private ArrayList<String> urlList;
    private ListView listView;
    private ImageView imageView;
    private SharedPreferences USER;
    private TextView userName;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                handler.sendEmptyMessage(2);
                listView.setAdapter(new KaijiangAdapter(MainActivity.this,kaiJiangInfoArrayList));
            }
            else if(msg.what==2){
                if(shapeLoadingDialog!=null){
                    shapeLoadingDialog.dismiss();
                }


            }else if(msg.what==3){
                startActivity(new Intent(MainActivity.this,MapActivity.class));
            }else if(msg.what==4){
                startActivity(new Intent(MainActivity.this,NewsActivity.class));
            }else if(msg.what==5){
                startActivity(new Intent(MainActivity.this,ZoushiActivty.class));
            }else if(msg.what==6){
                startActivity(new Intent(MainActivity.this,WanFaActivity.class));
            }else if(msg.what==7){
                shapeLoadingDialog.dismiss();
                Toast.makeText(MainActivity.this,"当前为最新版本\n版本号：1.1.0",Toast.LENGTH_LONG).show();
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        JPushInterface.init(getApplicationContext());
        setTitle("最新开奖");
        /*Intent intent = getIntent();

        if(intent.getExtras()!=null){
            String message = intent.getStringExtra("message");
            setCostomMsg(message);
        }*/

        USER=getSharedPreferences("USER",MODE_PRIVATE);
        listView=findViewById(R.id.listview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tag=kaiJiangInfoArrayList.get(position).getKaijiangName();
                Intent intent=new Intent(MainActivity.this,HistoryActivity.class);
                intent.putExtra("TAG",tag);
                startActivity(intent);
            }
        });
        shapeLoadingDialog = new ShapeLoadingDialog.Builder(this)
                .loadText("给时间一点时间...")
                .build();
        shapeLoadingDialog.setCanceledOnTouchOutside(false);
        shapeLoadingDialog.show();
        initData();
        getLotteryData();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,NewsActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Log.d("lee",navigationView.getHeaderCount()+"getHeaderCount");
        View headerView = navigationView.getHeaderView(0);
        imageView=headerView.findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("lee","R.id.imageView==item.getItemId()");
                getImage();
            }
        });
        String image = USER.getString("IMAGE", "");
        if(!TextUtils.isEmpty(image)){
            try {
                imageView.setImageBitmap(BitmapFactory.decodeFile(image));
            }catch (Exception e){

            }
        }
        userName=headerView.findViewById(R.id.tv_username);
        userName.setText("用户昵称："+USER.getString("USERNAME",""));
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(USER.getString("USERNAME",""));
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * 登陆注册
     */
    private void login(String name) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog, null);
        final EditText username = view.findViewById(R.id.username);
        if(!TextUtils.isEmpty(name)){
            username.setText(name);
        }
        AlertDialog.Builder  builder=new AlertDialog.Builder(this);
        builder.setTitle("填写信息");
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(username.getText())){
                    Toast.makeText(MainActivity.this,"昵称不能为空",Toast.LENGTH_SHORT).show();
                }else{
                    USER.edit().putString("USERNAME",username.getText().toString().trim()).commit();
                    userName.setText("用户昵称："+username.getText().toString().trim());
                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();

                    alertDialog.dismiss();
                }
            }
        });
    }
    AlertDialog alertDialog = null;
    private void setCostomMsg( String msg) {
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle("提示信息");
        builder.setMessage(msg);
        builder.setIcon(R.drawable.btn_about_on);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }
    private void getImage(){
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //这里要传一个整形的常量RESULT_LOAD_IMAGE到startActivityForResult()方法。
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            //查询我们需要的数据
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            USER.edit().putString("IMAGE",picturePath).commit();

        }
    }

    private void initData() {
        urlList=new ArrayList<>();
        urlList.add("cqssc");
        urlList.add("qxc");
        urlList.add("zcjqc");
        urlList.add("dlt");
        urlList.add("fc3d");
        urlList.add("pl3");
        urlList.add("pl5");
        urlList.add("qlc");
        urlList.add("ssq");

        SharedPreferences caipiao = getSharedPreferences("CAIPIAO", Context.MODE_PRIVATE);
        caipiao.edit().putString("重庆时时彩 - 高频", "http://f.apiplus.net/cqssc-10.json").commit();
        caipiao.edit().putString("黑龙江时时彩 - 高频", "http://f.apiplus.net/hljssc-10.json").commit();
        caipiao.edit().putString("新疆时时彩 - 高频", "http://f.apiplus.net/xjssc-10.json").commit();
        caipiao.edit().putString("超级大乐透", "http://f.apiplus.net/dlt-10.json").commit();
        caipiao.edit().putString("福彩3d", "http://f.apiplus.net/fc3d-10.json").commit();
        caipiao.edit().putString("排列3", "http://f.apiplus.net/pl3-10.json").commit();
        caipiao.edit().putString("排列5", "http://f.apiplus.net/pl5-10.json").commit();
        caipiao.edit().putString("七乐彩", "http://f.apiplus.net/qlc-10.json").commit();
        caipiao.edit().putString("七星彩", "http://f.apiplus.net/qxc-10.json").commit();
        caipiao.edit().putString("双色球", "http://f.apiplus.net/ssq-10.json").commit();
        caipiao.edit().putString("六场半全场", "http://f.apiplus.net/zcbqc-10.json").commit();
        caipiao.edit().putString("四场进球彩", "http://f.apiplus.net/zcjqc-10.json").commit();
        caipiao.edit().putString("安徽11选5 - 高频", "http://f.apiplus.net/ah11x5-10.json").commit();
        caipiao.edit().putString("北京11选5 - 高频", "http://f.apiplus.net/bj11x5-10.json").commit();
        caipiao.edit().putString("福建11选5 - 高频", "http://f.apiplus.net/fj11x5-10.json").commit();
        caipiao.edit().putString("广东11选5 - 高频", "http://f.apiplus.net/gd11x5-10.json").commit();
        caipiao.edit().putString("甘肃11选5 - 高频", "http://f.apiplus.net/gs11x5-10.json").commit();
        caipiao.edit().putString("广西11选5 - 高频", "http://f.apiplus.net/fx11x5-10.json").commit();

        SharedPreferences caipiaoName = getSharedPreferences("CAIPIAONAME", Context.MODE_PRIVATE);
        caipiaoName.edit().putInt("dlt",R.drawable.dlt).commit();
        caipiaoName.edit().putInt("cqssc",R.drawable.cqssc).commit();
        caipiaoName.edit().putInt("qxc",R.drawable.qxc).commit();
        caipiaoName.edit().putInt("zcjqc",R.drawable.zcjqc).commit();
        caipiaoName.edit().putInt("fc3d",R.drawable.fc3d).commit();
        caipiaoName.edit().putInt("pl3",R.drawable.pl3).commit();
        caipiaoName.edit().putInt("pl5",R.drawable.pl5).commit();
        caipiaoName.edit().putInt("qlc",R.drawable.qlc).commit();
        caipiaoName.edit().putInt("ssq",R.drawable.ssq).commit();
    }

    private RequestQueue mRequestQueue  = null;
    private ArrayList<KaiJiangInfo> kaiJiangInfoArrayList;
    private void getLotteryData(){
        if(!CheckUtil.isNetworkAvailable(MainActivity.this)){
            Toast.makeText(MainActivity.this,"没有检测到数据连接，请检查设备网络状态！",Toast.LENGTH_SHORT).show();
            handler.sendEmptyMessage(2);
            return;
        }
        kaiJiangInfoArrayList=new ArrayList<>();
        new Thread(){
            @Override
            public void run() {
                mRequestQueue = Volley.newRequestQueue(MainActivity.this);
                for (int k=0;k<urlList.size();k++){
                    String name = urlList.get(k);
                    String url="http://f.apiplus.net/"+name+"-1.json";
                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Log.d("lee", s);
                            List<KaiJiangInfo> kaiJiangInfos = ParseJsonUtil.ParseKaijiang(s);
                            if(kaiJiangInfos!=null&&kaiJiangInfos.size()>0){
                                kaiJiangInfoArrayList.add(kaiJiangInfos.get(0));
                            }else{
                                kaiJiangInfoArrayList.add(new KaiJiangInfo("06,10,21,28,29,31+12","2018-04-15 21:18:20","2018042","ssq"));
                            }
                            Log.d("lee","kaiJiangInfos:"+kaiJiangInfoArrayList.size());
                            if(kaiJiangInfoArrayList.size()==9){
                                handler.sendEmptyMessage(1);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d("lee", volleyError.toString());
                            handler.sendEmptyMessage(2);
                            Toast.makeText(MainActivity.this,"数据获取失败，请检查网络",Toast.LENGTH_SHORT).show();
                        }
                    }) ;
                    mRequestQueue.add(request);
                }

            }
        }.start();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_map){
            //地图
            handler.sendEmptyMessageDelayed(3,300);

        }else if (id==R.id.nav_news) {
            //新闻
            handler.sendEmptyMessageDelayed(4,300);

        } else if (id == R.id.nav_zoushi) {
            //走势
            handler.sendEmptyMessageDelayed(5,300);

        } else if (id == R.id.nav_wanfa) {
            //玩法
            handler.sendEmptyMessageDelayed(6,300);

        }  else if (id == R.id.nav_send) {
            shouDialog("检查可用更新...");
            handler.sendEmptyMessageDelayed(7,1800);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shouDialog(String string) {
        shapeLoadingDialog = new ShapeLoadingDialog.Builder(this)
                .loadText(string)
                .build();
        shapeLoadingDialog.setCanceledOnTouchOutside(false);
        shapeLoadingDialog.show();
    }

}
