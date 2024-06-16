package com.example.taskwiserebirth;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.taskwiserebirth.database.MongoDbRealmHelper;
import com.example.taskwiserebirth.database.TaskDatabaseManager;
import com.example.taskwiserebirth.task.TaskModel;
import com.example.taskwiserebirth.task.TaskAdapter;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.example.taskwiserebirth.utils.DialogUtils;
import com.example.taskwiserebirth.utils.PopupMenuUtils;

import io.realm.mongodb.App;
import io.realm.mongodb.User;


public class TaskDetailFragment extends Fragment implements TaskAdapter.TaskActionListener, TaskDatabaseManager.TaskUpdateListener {

    private TaskModel task;
    private TaskDatabaseManager taskDatabaseManager;
    private DialogUtils dialogUtils;


    public TaskDetailFragment (TaskModel task) {
        this.task = task;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);

        App app = MongoDbRealmHelper.initializeRealmApp();
        User user = app.currentUser();
        taskDatabaseManager = new TaskDatabaseManager(user, requireContext());
        dialogUtils = new DialogUtils(requireActivity(), taskDatabaseManager);

        ImageView backArrowImageView = rootView.findViewById(R.id.back_arrow);
        backArrowImageView.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        ImageView taskDetailMenu = rootView.findViewById(R.id.taskDetailMenu);
        taskDetailMenu.setOnClickListener(v -> PopupMenuUtils.showPopupMenu(requireContext(), v, task, this, requireActivity()));


        NestedScrollView nestedScrollView = rootView.findViewById(R.id.nestedScrollViewTaskDetail);
        nestedScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                boolean scrollingUp = scrollY < oldScrollY;
                ((MainActivity) requireActivity()).toggleNavBarVisibility(scrollingUp, true);
            }
        });

        setUpTaskDetail(rootView);

        return rootView;
    }

    private void setUpTaskDetail(View view) {
        TextView taskName = view.findViewById(R.id.taskNameDetail);
        TextView sched = view.findViewById(R.id.schedDetail);
        TextView deadline = view.findViewById(R.id.deadlineDetail);
        TextView importance = view.findViewById(R.id.importanceDetail);
        TextView urgency = view.findViewById(R.id.urgencyDetail);
        TextView recurrence = view.findViewById(R.id.recurrenceDetail);
        TextView notes = view.findViewById(R.id.notesDetailText);
        CheckBox reminder = view.findViewById(R.id.remindMeCheckBox);

        taskName.setText(task.getTaskName());
        sched.setText(CalendarUtils.formatDeadline(task.getSchedule()));
        deadline.setText(CalendarUtils.formatDeadline(task.getDeadline()));
        importance.setText(task.getImportanceLevel());
        urgency.setText(task.getUrgencyLevel());
        recurrence.setText(task.getRecurrence());
        reminder.setChecked(task.isReminder());

        if (!task.getRecurrence().equalsIgnoreCase("none")) {
            deadline.setText("Repeating");
        }

        notes.setText(task.getNotes().isEmpty() ? "No notes" : task.getNotes());
        notes.setTypeface(null, task.getNotes().isEmpty() ? Typeface.BOLD : Typeface.NORMAL);
    }

    @Override
    public void onEditTask(TaskModel task) {
        dialogUtils.showBottomSheetDialog(task, this);
    }

    @Override
    public void onDeleteTask(TaskModel task) {
        taskDatabaseManager.deleteTask(task);
        Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDoneTask(TaskModel task) {
        taskDatabaseManager.markTaskAsFinished(task);
        Toast.makeText(getContext(), "Task status updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskUpdated(TaskModel updatedTask) {
        this.task = updatedTask;
        setUpTaskDetail(getView());
    }
}
