package tcb;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
 
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.da.AccessBase;
import org.openscada.opc.lib.da.DataCallback;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.ItemState;
import org.openscada.opc.lib.da.Server;
import org.openscada.opc.lib.da.SyncAccess;
 
public class UtgardTutorial2 {
    
    public static void main(String[] args) throws Exception {
 
        // ������Ϣ 
        final ConnectionInformation ci = new ConnectionInformation();
        
        ci.setHost("192.168.0.1");          // ����IP
        ci.setDomain("");                   // ��Ϊ�վ���
        ci.setUser("OPCUser");              // �û���������DCOMʱ���õ�
        ci.setPassword("123456");           // ����
        
        // ʹ��MatrikonOPC Server������
        // ci.setClsid("F8582CF2-88FB-11D0-B850-00C0F0104305"); // MatrikonOPC��ע���ID�������ڡ���������￴��
        // final String itemId = "u.u";    // ������ְ�ʵ��
 
        // ʹ��KEPServer������
        ci.setClsid("7BC0CC8E-482C-47CA-ABDC-0FE7F9C6E729"); // KEPServer��ע���ID�������ڡ���������￴��
        final String itemId = "u.u.u";    // ������ְ�ʵ�ʣ�û��ʵ��PLC���õ�ģ������simulator
        // final String itemId = "ͨ�� 1.�豸 1.��� 1";
        
        // create a new server����������
        final Server server = new Server(ci, Executors.newSingleThreadScheduledExecutor());
        try {
            // connect to server�����ӵ�����
            server.connect();
 
            // add sync access, poll every 500 ms������һ��ͬ����access������ȡ��ַ�ϵ�ֵ���̳߳�ÿ500ms��ֵһ��
            // ���������ѭ����ֵ�ģ�ֻ��һ��ֵ��������
            final AccessBase access = new SyncAccess(server, 500);
            // ���Ǹ��ص����������Ƕ���ֵ��ִ����ִ������Ĵ��룬����������д�ģ���ȻҲ����д������ȥ
            access.addItem(itemId, new DataCallback() {
                @Override
                public void changed(Item item, ItemState state) {
                    // also dump value
                    try {
                        if (state.getValue().getType() == JIVariant.VT_UI4) { // ���������ֵ����ʱUnsignedInteger�����޷���������ֵ
                            System.out.println("<<< " + state + " / value = " + state.getValue().getObjectAsUnsigned().getValue());
                        } else {
                            System.out.println("<<< " + state + " / value = " + state.getValue().getObject());
                        }
                    } catch (JIException e) {
                        e.printStackTrace();
                    }
                }
            });
 
            // Add a new group�����һ���飬��������Ͷ�ֵ����дֵһ�Σ�������ѭ����ȡ����д��
            // ����������⣬��������������Ϊ��server����addGroupҲ����removeGroup����һ��ֵ����������飬Ȼ���Ƴ��飬�ٶ�һ�ξ������Ȼ��ɾ��
            final Group group = server.addGroup("test"); 
            // Add a new item to the group��
            // ��һ��item���뵽�飬item���־���MatrikonOPC Server����KEPServer���潨��������ֱ��磺u.u.TAG1��PLC.S7-300.TAG1
            final Item item = group.addItem(itemId);
 
            // start reading����ʼѭ����ֵ
            access.bind();
 
            // add a thread for writing a value every 3 seconds
            // д��һ�ξ���item.write(value)��ѭ��д�������߳�һֱִ��item.write(value)
            ScheduledExecutorService writeThread = Executors.newSingleThreadScheduledExecutor();
            writeThread.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    final JIVariant value = new JIVariant("24");  // д��24
                  
                    try {
                        System.out.println(">>> " + "д��ֵ��  " + "24");
                        item.write(value);
                    } catch (JIException e) {
                        e.printStackTrace();
                    }
                }
            }, 5, 3, TimeUnit.SECONDS); // ������5���һ��ִ�д��룬�Ժ�ÿ3��ִ��һ�δ���
 
            // wait a little bit ����ʱ20��
            Thread.sleep(20 * 1000);
            writeThread.shutdownNow();  // �ص�һֱд����߳�
            // stop reading��ֹͣѭ����ȡ��ֵ
            access.unbind();
        } catch (final JIException e) {
            System.out.println(String.format("%08X: %s", e.getErrorCode(), server.getErrorMessage(e.getErrorCode())));
        }
    }
}