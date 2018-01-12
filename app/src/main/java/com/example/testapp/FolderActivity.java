package com.example.testapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.GetFilesUtils;
//import wuwang.ebookworm.R;
//import wuwang.tools.utils.GetFilesUtils;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FolderActivity extends Activity implements OnItemClickListener,OnClickListener {

    private ListView folderLv;
    private TextView foldernowTv;
    private SimpleAdapter sAdapter;
    private List<Map<String, Object>> aList;
    private String baseFile;

    private TextView titleTv;

    // 声明一个数组，用来存储所有需要动态申请的权限
    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    private List<String> mPermissionList = new ArrayList<>();


    private void checkPermission() {
        mPermissionList.clear();
//        /**
//         * 判断哪些权限未授予
//         * 以便必要的时候重新申请
//         */
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(FolderActivity.this, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
        /**
         * 判断存储委授予权限的集合是否为空
         */
        if (!mPermissionList.isEmpty()) {
            String [] permissions1 = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(FolderActivity.this, permissions1, 1);
        } else {//未授予的权限为空，表示都授予了
            // 后续操作...
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder_layout);
//        if(ContextCompat.checkSelfPermission(FolderActivity.this, Manifest.
//        permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        checkPermission();
        baseFile=GetFilesUtils.getInstance().getBasePath();

//        titleTv=(TextView) findViewById(R.id.title_text);
//        titleTv.setText("本地文件");
        folderLv=(ListView) findViewById(R.id.folder_list);
        foldernowTv=(TextView) findViewById(R.id.folder_now);
        Drawable drawable1 = getResources().getDrawable(R.drawable.folder_backupimg);
        drawable1.setBounds(0,0,80,80);
        foldernowTv.setCompoundDrawables(drawable1, null, null, null);
        foldernowTv.setText(baseFile);
        foldernowTv.setOnClickListener(this);
        aList=new ArrayList<Map<String,Object>>();
        sAdapter=new SimpleAdapter(this, aList,R.layout.listitem_folder, new String[]{"fImg","fName","fInfo"},
                new int[]{R.id.folder_img,R.id.folder_name,R.id.folder_info});
        folderLv.setAdapter(sAdapter);
        folderLv.setOnItemClickListener(this);
        try {
            loadFolderList(baseFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void loadFolderList(String file) throws IOException{
        List<Map<String, Object>> list=GetFilesUtils.getInstance().getSonNode(file);
        if(list!=null){
            Collections.sort(list, GetFilesUtils.getInstance().defaultOrder());
            aList.clear();
            for(Map<String, Object> map:list){
                String fileType=(String) map.get(GetFilesUtils.FILE_INFO_TYPE);
                String fName = map.get(GetFilesUtils.FILE_INFO_NAME).toString();
                if(fName.substring(0,1).equals(".")){
                    continue;
                }
                Map<String,Object> gMap=new HashMap<String, Object>();
                if(map.get(GetFilesUtils.FILE_INFO_ISFOLDER).equals(true)){
                    gMap.put("fIsDir", true);
                    gMap.put("fImg",R.drawable.filetype_folder );
                    gMap.put("fInfo", map.get(GetFilesUtils.FILE_INFO_NUM_SONDIRS)+"个文件夹和"+
                            map.get(GetFilesUtils.FILE_INFO_NUM_SONFILES)+"个文件");
                }else{
                    gMap.put("fIsDir", false);
                    if(fileType.equals("txt")||fileType.equals("text")){
                        gMap.put("fImg", R.drawable.filetype_txt);
                    }else{
                        gMap.put("fImg", R.drawable.filetype_unknow);
                    }
                    gMap.put("fInfo","文件大小:"+GetFilesUtils.getInstance().getFileSize(
                            map.get(GetFilesUtils.FILE_INFO_PATH).toString()));
                }
                gMap.put("fName", map.get(GetFilesUtils.FILE_INFO_NAME));
                gMap.put("fPath", map.get(GetFilesUtils.FILE_INFO_PATH));
                aList.add(gMap);
            }
        }else{
            aList.clear();
        }
        sAdapter.notifyDataSetChanged();
        foldernowTv.setText(file);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        // TODO Auto-generated method stub
        try {
            if(aList.get(position).get("fIsDir").equals(true)){
                loadFolderList(aList.get(position).get("fPath").toString());
            }else{
                Toast.makeText(this, "这是文件，处理程序待添加", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if(v.getId()==R.id.folder_now){
            try {
                String folder=GetFilesUtils.getInstance().getParentPath(foldernowTv.getText().toString());
                if(folder==null){
                    Toast.makeText(this, "无父目录，待处理", Toast.LENGTH_SHORT).show();
                }else{
                    loadFolderList(folder);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    boolean mShowRequestPermission = true;//用户是否禁止权限,可通过标识进行退出

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if(grantResults.length>0){
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //判断是否勾选禁止后不再询问
//                            boolean showRequestPermission = ActivityCompat.
//                                    shouldShowRequestPermissionRationale(FolderActivity.this,
//                                            permissions[i]);
//                            if (showRequestPermission) {
//                                // 为true的时候，显示对话框对该权限说明，并让用户选择是否再次申请权限
//                            } else {
//                                // 后续操作...
//                            }
                            Toast.makeText(this, "不同意权限将退出", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }

                // 授权结束后的后续操作...
                break;
            default:
                break;
        }
    }


}