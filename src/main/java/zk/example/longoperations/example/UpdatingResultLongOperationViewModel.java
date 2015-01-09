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
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.LongOperation;

public class UpdatingResultLongOperationViewModel {

	private static final String IDLE = "idle";
	private String status = IDLE;

	private ListModelList<String> resultModel = new ListModelList<String>();

	@Command
	public void startLongOperation() {

		final List<String> input = Arrays.asList("password", "secret", "telephone", "monster", "tree", "banana", "hunter", "greetings", "wares");

		LongOperation longOperation = new LongOperation() {

			private MessageDigest md5;

			@Override
			protected void execute() throws InterruptedException {
				try {
					md5 = MessageDigest.getInstance("MD5");
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				}
				Queue<String> stringsToHash = new LinkedList<String>(input);
				while(stringsToHash.peek() != null) {
					processNext(stringsToHash, 2);
				}
				return;
			}

			@Override
			protected void onException(RuntimeException exception) {
				Clients.showNotification("Error executing operation: " + exception, Clients.NOTIFICATION_TYPE_ERROR, null, null, 1000);
			}

			@Override
			protected void onCleanup() {
				updateStatus(IDLE);
			}

			private void processNext(Queue<String> stringsToHash, int count) throws InterruptedException {
				Map<String, String> hashes = new LinkedHashMap<String, String>(count);
				for(int i = 0; i < count; i++) {
					String string = stringsToHash.poll();
					if(string != null) {
						hashes.put(string, hash(string));
					} else {
						break;
					}
				}
				Thread.sleep(500);
				updateResults(hashes);
			}


			private void updateResults(Map<String, String> hashes) throws InterruptedException {
				activate();
				for (Entry<String, String> entry : hashes.entrySet()) {
					resultModel.add(entry.getKey() + " -> " + entry.getValue());
				}
				deactivate();
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
		longOperation.start();
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