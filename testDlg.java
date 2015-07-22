import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;

/**
 * 通过GUI实现了文件的上传、查看和删除功能
 */
public class testDlg extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JTextField textField2;
    private JTree tree1;
    private JButton updateButton;
    private JCheckBox checkBox1;

    public testDlg() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        updateButton.addActionListener(e -> updateTree());



        updateTree();
        tree1.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

                TreePath path = e.getPath();    //返回的是一个TreeNode型数组
                Object[] path1 = path.getPath();
                String filePath = "";
                for(int i=0; i<path1.length; i++){
                    if(i == 0){
                        filePath += "/";
                    }
                    else{
                        filePath += "/"+path1[i];
                    }
                }
                filePath = "hdfs://localhost:9000"+filePath;

                if(checkBox1.isSelected()) {
                    Configuration conf = new Configuration();
                    try {
                        FileSystem hdfs = FileSystem.get(URI.create("hdfs://localhost:9000/"), conf);
                        Path delef = new Path(filePath);
                        boolean isDeleted = hdfs.delete(delef, true);
                        System.out.println("Delete?\n" + isDeleted);
                    } catch (IOException ee) {
                        ee.printStackTrace();
                    }
                }
                else{
                    System.out.println(filePath);
                }



//                File file = new File(e.getPath().toString());
//                file.delete();

            }
        });
    }

//    private DefaultMutableTreeNode deleteHDFS_file(FileSystem hdfs,String filePath) throws IOException {
//        if(hdfs.isDirectory(new Path(filePath))){
//            hdfs.delete(new Path(filePath),true);
////            FileStatus[] statuses = hdfs.listStatus(new Path(filePath));
////            for(FileStatus status : statuses){
////                deleteHDFS_file(FileSystem hdfs, status.getPath().toString());
////            }
//        }
//        else
//            hdfs.delete(new Path(filePath),)
//
//    }

    private void onOK() {
        String localFile = textField1.getText();
        String HDFSFile = textField2.getText();
        uploadFile(localFile, HDFSFile);

  //      dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    private void uploadFile(String localFile,String HDFS_File){
        Configuration conf;
        try {
            conf = new Configuration();
            FileSystem hdfs = FileSystem.get(URI.create("hdfs://localhost:9000/"), conf);
            FileSystem local = FileSystem.getLocal(conf);
            Path inputDir = new Path(localFile);
            Path hdfsFile = new Path(HDFS_File);

            FSDataOutputStream out;
            FSDataInputStream in = local.open(inputDir);
            out = hdfs.create(hdfsFile);

            byte buffer[] = new byte[256];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            in.close();
            System.out.println("end");

        }catch (Exception e) {
            System.out.println(e.toString());
        }
    }



    private void updateTree() {

        Configuration conf;
        try {
            conf = new Configuration();
            FileSystem hdfs = FileSystem.get(URI.create("hdfs://localhost:9000/"), conf);
            FileStatus[] status = hdfs.listStatus(new Path("/"));
            DefaultMutableTreeNode top = getFile2node(hdfs, status, "/");
            DefaultTreeModel treeModel = new DefaultTreeModel(top);
            tree1.setModel(treeModel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private DefaultMutableTreeNode getFile2node(FileSystem hdfs,FileStatus[] status,String name) throws IOException {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(name);
        for(FileStatus status1 : status){
            DefaultMutableTreeNode node;
            if(status1.isDirectory()){
                node = getFile2node(hdfs,hdfs.listStatus(status1.getPath()),status1.getPath().getName());
            }
            else {
                node = new DefaultMutableTreeNode(status1.getPath().getName());
            }
            top.add(node);
        }
        return top;
    }



    public static void main(String[] args) {
        testDlg dialog = new testDlg();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}