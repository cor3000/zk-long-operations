package zk.example.longoperations.example;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.lang.Threads;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.api.LongOperation;
import zk.example.longoperations.api.UpdatableLongOperation;

public class ParallelLongOperationViewModel {

	ListModelList<Task> resultModel = new ListModelList<Task>();
	
    @Command
    public void startLongOperation() {
        LongOperation<Task, Task> longOperation = new UpdatableLongOperation<Task, Task, Task>() {
        	@Override
        	protected Task execute(Task task) {
        		for(int i = 0; i <= 5; i++) {
        			task.setProgress(i * 20);
        			update(task);
        			Threads.sleep(1500);
        		}
				return task;
            }
            
        	@Override
        	protected void onUpdate(Task task) {
        		BindUtils.postNotifyChange(null,  null, task, "progress");
        	}
        	
            @Override
            protected void onFinish(Task task) {
            	resultModel.remove(task);
            }
        };
        
        Task task = new Task("task-" + System.currentTimeMillis());
        resultModel.add(task);
		longOperation.start(task);
    }

	public ListModelList<Task> getResultModel() {
		return resultModel;
	}
	
	public static class Task {
		private String name;
		private int progress;
		
		public Task(String name) {
			this.name = name;
		}
		public int getProgress() {
			return progress;
		}
		public void setProgress(int progress) {
			this.progress = progress;
		}
		public String getName() {
			return name;
		}
	}
}