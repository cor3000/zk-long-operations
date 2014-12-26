package zk.example.longoperations.example;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import javax.xml.bind.DatatypeConverter;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.lang.Threads;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.api.LongOperation;
import zk.example.longoperations.api.UpdatableLongOperation;

public class UpdatingResultLongOperationViewModel {

	private ListModelList<String> resultModel = new ListModelList<String>();
	
	private String status = "idle";
	
    @Command
    public void startLongOperation() {

        LongOperation<List<String>, Void> longOperation = new UpdatableLongOperation<List<String>, Map<String, String>, Void>() {

        	private MessageDigest md5;

        	@Override
        	protected Void execute(List<String> input) {
        		try {
					md5 = MessageDigest.getInstance("MD5");
				} catch (NoSuchAlgorithmException e) {
					Clients.showNotification("Error executing operation", Clients.NOTIFICATION_TYPE_ERROR, null, null, 1000);
					e.printStackTrace();
					return null;
				}
        		Queue<String> stringsToHash = new LinkedList<String>(input);
        		while(stringsToHash.peek() != null) {
        			processNext(stringsToHash, 2);
        		}
				return null;
            }

            private void processNext(Queue<String> stringsToHash, int count) {
            	Map<String, String> hashes = new LinkedHashMap<String, String>(count);
            	for(int i = 0; i < count; i++) {
            		String string = stringsToHash.poll();
            		if(string != null) {
            			hashes.put(string, hash(string));
            		} else {
            			break;
            		}
            	}
           		Threads.sleep(500);
            	update(hashes);
			}


            @Override
            protected void onUpdate(Map<String, String> hashes) {
            	for (Entry<String, String> entry : hashes.entrySet()) {
            		resultModel.add(entry.getKey() + " -> " + entry.getValue());
				}
            }
            
            @Override
            protected void onFinish(Void result) {
            	updateStatus("idle");
            }

            private String hash(String string) {
            	byte[] digest;
            	try {
            		digest = md5.digest(string.getBytes("utf-8"));
            		return DatatypeConverter.printHexBinary(digest);
            	} catch (UnsupportedEncodingException e) {
            		throw new RuntimeException("should not happen");
            	}
            }
        };
        updateStatus("processing");
        longOperation.start(Arrays.asList("password", "secret", "telephone", "monster", "tree", "banana", "hunter", "greetings", "wares"));
    }

    private void updateStatus(String update) {
    	status = update;
    	BindUtils.postNotifyChange(null, null, UpdatingResultLongOperationViewModel.this, "status");
    }

	public ListModelList<String> getResultModel() {
		return resultModel;
	}

	public String getStatus() {
		return status;
	}
}