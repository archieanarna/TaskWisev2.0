package com.example.taskwiserebirth.task;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskwiserebirth.R;
import com.example.taskwiserebirth.TaskDetailFragment;
import com.example.taskwiserebirth.utils.CalendarUtils;
import com.example.taskwiserebirth.utils.PopupMenuUtils;

import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FOLDER_EXPANDED = 0;
    private static final int TYPE_FOLDER_COLLAPSED = 1;
    private static final int TYPE_TASK = 2;
    private final Context context;
    private List<TaskModel> tasks;
    private final TaskActionListener actionListener;
    private Date selectedDate;
    private FragmentActivity activity;
    private final int closeToDueHours = 12; //TODO: edit close to due to be similar to CalendarUtils.calculateCloseToDue
    private double highestPriorityScore;

    public interface TaskActionListener {
        void onEditTask(TaskModel task);
        void onDeleteTask(TaskModel task);
        void onDoneTask(TaskModel task);
    }

    public TaskAdapter(Context context, FragmentActivity activity, List<TaskModel> tasks, TaskActionListener listener) {
        this.context = context;
        this.activity = activity;
        this.tasks = tasks;
        this.actionListener = listener;
        this.selectedDate = new Date();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOLDER_EXPANDED || viewType == TYPE_FOLDER_COLLAPSED) {
            // Inflate the appropriate layout based on the expanded state
            View itemView = LayoutInflater.from(context).inflate(
                    viewType == TYPE_FOLDER_EXPANDED ? R.layout.item_folder_expanded : R.layout.item_folder_collapsed,
                    parent,
                    false
            );
            return new FolderViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_task_cards, parent, false);
            return new TaskViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskModel currentTask = tasks.get(position);

        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));

        if (holder instanceof FolderViewHolder) {
            FolderViewHolder folderHolder = (FolderViewHolder) holder;
            folderHolder.folderName.setText(generateFolderName(currentTask.getChildTasks()));
            folderHolder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
            TaskAdapter nestedAdapter = new TaskAdapter(context, activity, currentTask.getChildTasks(), actionListener);
            folderHolder.recyclerView.setAdapter(nestedAdapter);

            folderHolder.recyclerView.setVisibility(currentTask.isExpanded() ? View.VISIBLE : View.GONE);

            folderHolder.itemView.setOnClickListener(v -> {
                currentTask.setExpanded(!currentTask.isExpanded());
                notifyItemChanged(position);
            });

            // Set the highest priority icon visibility for tasks within the folder
            nestedAdapter.setHighestPriorityScore(highestPriorityScore);
            nestedAdapter.notifyDataSetChanged();

            boolean hasHighestPriorityTask = false;
            for (TaskModel task : currentTask.getChildTasks()) {
                if (task.getPriorityScore() == highestPriorityScore && task.getStatus().equalsIgnoreCase("unfinished")) {
                    hasHighestPriorityTask = true;
                    break;
                }
            }
            folderHolder.topPriorityIcon.setVisibility(hasHighestPriorityTask ? View.VISIBLE : View.INVISIBLE);
        } else if (holder instanceof TaskViewHolder) {
            TaskViewHolder taskHolder = (TaskViewHolder) holder;
            int taskNameColor = getTaskCategoryColor(currentTask);
            int priorityColor = getTaskPriorityColor(currentTask);

            taskHolder.taskName.setText(currentTask.getTaskName());
            taskHolder.taskName.setTextColor(taskNameColor);

            // setting priority
            if (currentTask.getStatus().equalsIgnoreCase("unfinished")) {
                taskHolder.priority.setText(currentTask.getPriorityCategory());
                taskHolder.priority.setTextColor(priorityColor);
                Typeface typeface = getTypefaceForPriority(currentTask.getPriorityCategory());
                taskHolder.priority.setTypeface(typeface);
            } else {
                taskHolder.priority.setText("Done");
                taskHolder.priority.setTextColor(ContextCompat.getColor(context, R.color.green));
                taskHolder.priority.setTypeface(ResourcesCompat.getFont(context, R.font.inter_semibold));
            }

            // set card color if past due
            Date taskDeadline = CalendarUtils.parseDeadline(currentTask.getDeadline());
            if (currentTask.getStatus().equalsIgnoreCase("unfinished") && taskDeadline != null && taskDeadline.before(selectedDate)) {
                taskHolder.cardBg.setBackgroundResource(R.drawable.bg_card_past_due);
                taskHolder.taskCard.setCardElevation(0);
                taskHolder.priority.setText("Past Due");
                taskHolder.priority.setTextColor(ContextCompat.getColor(context, R.color.ash_gray));
                taskHolder.priority.setTypeface(ResourcesCompat.getFont(context, R.font.inter_semibold));
            } else {
                taskHolder.cardBg.setBackgroundResource(android.R.color.transparent);
                taskHolder.taskCard.setCardElevation(4 * context.getResources().getDisplayMetrics().density);
            }

            String recurrence = currentTask.getRecurrence();
            if (!recurrence.equalsIgnoreCase("none")) {
                taskHolder.schedOrDeadlineTxt.setText(currentTask.getSchedule());
                taskHolder.recurrenceTxt.setText(recurrence);
                taskHolder.recurrenceIcon.setVisibility(View.VISIBLE);
            } else {
                taskHolder.schedOrDeadlineTxt.setText(currentTask.getDeadline());
                taskHolder.recurrenceTxt.setText("");
                taskHolder.recurrenceIcon.setVisibility(View.INVISIBLE);
            }

            taskHolder.menuView.setOnClickListener(v -> PopupMenuUtils.showPopupMenu(context, v, currentTask, actionListener, activity));
            taskHolder.itemView.setOnClickListener(v -> {
                TaskDetailFragment fragmentViewerCard = new TaskDetailFragment(currentTask);
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.frame_layout, fragmentViewerCard, "TASK_DETAIL_FRAGMENT")
                        .addToBackStack(null)
                        .commit();
            });

            if (currentTask.getPriorityScore() == highestPriorityScore && currentTask.getStatus().equalsIgnoreCase("unfinished")) {
                taskHolder.topPriorityIcon.setVisibility(View.VISIBLE);
            } else {
                taskHolder.topPriorityIcon.setVisibility(View.INVISIBLE);
            }

        }
    }

    private Typeface getTypefaceForPriority(String priorityCategory) {
        if (priorityCategory == null) {
            return Typeface.DEFAULT;
        }

        switch (priorityCategory) {
            case TaskPriorityCalculator.PRIORITY_MEDIUM:
                return ResourcesCompat.getFont(context, R.font.inter_semibold); // Assuming you have this font
            case TaskPriorityCalculator.PRIORITY_HIGH:
                return ResourcesCompat.getFont(context, R.font.inter_bold); // Assuming you have this font
            case TaskPriorityCalculator.PRIORITY_VERY_HIGH:
                return ResourcesCompat.getFont(context, R.font.inter_extra_bold); // Assuming you have this font
            default:
                return ResourcesCompat.getFont(context, R.font.inter);
        }
    }

    private int  getTaskPriorityColor(TaskModel task) {
        switch (task.getPriorityCategory()) {
            case TaskPriorityCalculator.PRIORITY_LOW:
                return ContextCompat.getColor(context, R.color.priority_low);
            case TaskPriorityCalculator.PRIORITY_MEDIUM:
                return ContextCompat.getColor(context, R.color.priority_medium);
            case TaskPriorityCalculator.PRIORITY_HIGH:
                return ContextCompat.getColor(context, R.color.priority_high);
            case TaskPriorityCalculator.PRIORITY_VERY_HIGH:
                return ContextCompat.getColor(context, R.color.priority_very_high);
            default:
                return ContextCompat.getColor(context, R.color.priority_no_set);
        }
    }

    private int getTaskCategoryColor(TaskModel task) {
        if (task.getStatus().equals("Finished")) {
            return ContextCompat.getColor(context, R.color.green);
        } else {
            Date taskDeadline = CalendarUtils.parseDeadline(task.getDeadline());
            if (taskDeadline == null) {
                return ContextCompat.getColor(context, R.color.blue);
            } else {
                if (taskDeadline.before(selectedDate)) {
                    return ContextCompat.getColor(context, R.color.ash_gray);
                } else {
                    long diffMillis = taskDeadline.getTime() - selectedDate.getTime();
                    long diffHours = diffMillis / (60 * 60 * 1000); // millis to hours

                    if (diffHours <= closeToDueHours) {
                        return ContextCompat.getColor(context, R.color.red);
                    }
                }
            }
        }
        return ContextCompat.getColor(context, R.color.blue);
    }

    public void setHighestPriorityScore(double highestPriorityScore) {
        this.highestPriorityScore = highestPriorityScore;
    }

    @Override
    public int getItemViewType(int position) {
        TaskModel task = tasks.get(position);
        if (task.isExpandable()) {
            return task.isExpanded() ? TYPE_FOLDER_EXPANDED : TYPE_FOLDER_COLLAPSED;
        } else {
            return TYPE_TASK;
        }
    }

    private String generateFolderName(List<TaskModel> childTasks) {
        if (childTasks.isEmpty()) {
            return "Empty Folder";
        }

        StringBuilder folderNameBuilder = new StringBuilder();
        for (TaskModel child : childTasks) {
            folderNameBuilder.append(child.getTaskName()).append(", ");
        }

        // Remove the trailing comma and space
        if (folderNameBuilder.length() > 2) {
            folderNameBuilder.setLength(folderNameBuilder.length() - 2);
        }

        return folderNameBuilder.toString();
    }

    private void calculateHighestPriorityScore() {
        highestPriorityScore = 0;
        for (TaskModel task : tasks) {
            if (task.getPriorityScore() > highestPriorityScore) {
                highestPriorityScore = task.getPriorityScore();
            }
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<TaskModel> tasks) {
        this.tasks = tasks;
        calculateHighestPriorityScore();
        notifyDataSetChanged();
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }
}
