package com.pigmal.android.sample.bolts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import bolts.Continuation;
import bolts.Task;

public class MainFragment extends Fragment implements OnClickListener {
	static final String TAG = "MainFragment";
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		rootView.findViewById(R.id.button_single).setOnClickListener(this);
		rootView.findViewById(R.id.button_chaining).setOnClickListener(this);
		rootView.findViewById(R.id.button_series).setOnClickListener(this);
		rootView.findViewById(R.id.button_parallel).setOnClickListener(this);
		return rootView;
	}

	public void runSingleTask() {
		asyncMethod("Hello world").continueWith(new Continuation<String, Void>() {
			@Override
			public Void then(Task<String> task) throws Exception {
				Log.v(TAG, "Task finished : " + task.getResult());
				return null;
			}
		});
	}

	public void runChainingTasks() {
		asyncMethod("foo")
				.onSuccessTask(new Continuation<String, Task<String>>() {
					@Override
					public Task<String> then(Task<String> task) throws Exception {
						return asyncMethod(task.getResult() + "bar");
					}
				}).onSuccessTask(new Continuation<String, Task<String>>() {
					@Override
					public Task<String> then(Task<String> task) throws Exception {
						return asyncMethod(task.getResult() + "baz");
					}
				}).continueWith(new Continuation<String, Void>() {
					@Override
					public Void then(Task<String> task) throws Exception {
						Log.v(TAG, "Chain finished : " + task.getResult());
						return null;
					}
				});
	}

	List<String> list = Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");

	public void runSeriesTasks() {
		series(list).continueWith(new Continuation<String, String>() {
			@Override
			public String then(Task<String> task) throws Exception {
				Log.v(TAG, "All task finished in series : " + task.getResult());
				return null;
			}
		});
	}

	public void runParallelTasks() {
		parallel(list).continueWith(new Continuation<Void, String>() {
			@Override
			public String then(Task<Void> task) throws Exception {
				Log.v(TAG, "All tasks finished parallelly : " + task.getResult());
				return null;
			}
		});
	}

	public Task<String> series(List<String> results) {
		Task<String> task = Task.forResult(null);
		for (final String result : results) {
			task = task.continueWithTask(new Continuation<String, Task<String>>() {
				@Override
				public Task<String> then(Task<String> task) throws Exception {
					return asyncMethod(task.getResult() + ", " + result);
				}
			});
		}
		return task;
	}

	public Task<Void> parallel(List<String> results) {
		// Collect one task for each delete into an array.
		ArrayList<Task<String>> tasks = new ArrayList<Task<String>>();
		for (String result : results) {
			// Start this delete immediately and add its task to the list.
			tasks.add(asyncMethod(result));
		}
		// Return a new task that will be marked as completed when all of the
		// deletes are
		// finished.
		return Task.whenAll(tasks);
	}

	/**
	 * 非同期処理を行うメソッド。Taskを返す。
	 */
	public Task<String> asyncMethod(final String param) {
		final Task<String>.TaskCompletionSource task = Task.<String> create();
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.v(TAG, "Running task (" + param + ")");
				try {
					Thread.sleep(1 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				task.setResult(param);
				// エラーの場合はsetError()
				//task.setError(new RuntimeException("An error message."));
			}
		}).start();

		return task.getTask();
	}

	public void pyramidAsync() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// do something 1
				new Thread(new Runnable() {
					@Override
					public void run() {
						//do something 2
						new Thread(new Runnable() {
							@Override
							public void run() {
								// do something 3
							}							
						}).start();
					}			
				}).start();
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_single:
			runSingleTask();
			break;
		case R.id.button_chaining:
			runChainingTasks();
			break;
		case R.id.button_series:
			runSeriesTasks();
			break;
		case R.id.button_parallel:
			runParallelTasks();
			break;
		}
	}
}
