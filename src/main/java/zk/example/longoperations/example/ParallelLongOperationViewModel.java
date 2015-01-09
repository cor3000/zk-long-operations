package zk.example.longoperations.example;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.zul.ListModelList;

import zk.example.longoperations.LongOperation;

public class ParallelLongOperationViewModel {

	private ListModelList<TaskInfo> currentTasksModel = new ListModelList<TaskInfo>();

	@Command
	public void startLongOperation() {
		final TaskInfo task = new TaskInfo("task-" + System.currentTimeMillis());

		LongOperation longOperation = new LongOperation() {

			@Override
			protected void execute() throws InterruptedException {
				for(int i = 0; i <= 5; i++) {
					task.setProgress(i * 20);
					updateTaskProgress(task);
					Thread.sleep(1500);
				}
			}

			private void updateTaskProgress(TaskInfo task) throws InterruptedException {
				activate();
				BindUtils.postNotifyChange(null,  null, task, "progress");
				deactivate();
			}

			@Override
			protected void onCleanup() {
				currentTasksModel.remove(task);
			}
		};

		currentTasksModel.add(task);
		longOperation.start();
	}

	public ListModelList<TaskInfo> getCurrentTasksModel() {
		return currentTasksModel;
	}

	public static class TaskInfo {
		private String name;
		private int progress;

		public TaskInfo(String name) {
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