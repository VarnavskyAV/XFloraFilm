package com.alaka_ala.florafilm.ui.fragments.filmDetails;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaka_ala.florafilm.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Адаптер для иерархической структуры.
 * Модели данных (Folder, File) являются Serializable и самодостаточными.
 */
public class SelectorVoiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FOLDER = 0;
    private static final int TYPE_FILE = 1;

    private final Map<Integer, Folder> rootFolders = new TreeMap<>();
    private final List<Item> displayedItems = new ArrayList<>();
    private final OnFileClickListener fileClickListener;

    public SelectorVoiceAdapter(OnFileClickListener fileClickListener) {
        this.fileClickListener = fileClickListener;
    }

    public void clearData() {
        this.rootFolders.clear();
        rebuildDisplayedItems();
    }

    public void addData(List<Folder> newFolders) {
        if (newFolders == null || newFolders.isEmpty()) {
            return;
        }
        for (Folder newFolder : newFolders) {
            if (newFolder.getIndexPath() != null && !newFolder.getIndexPath().isEmpty()) {
                int balancerId = newFolder.getIndexPath().get(0);
                rootFolders.put(balancerId, newFolder);
            }
        }
        rebuildDisplayedItems();
    }

    public List<Folder> getRootFolders() {
        return new ArrayList<>(rootFolders.values());
    }

    private void rebuildDisplayedItems() {
        displayedItems.clear();
        for (Folder root : rootFolders.values()) {
            root.setLevel(0);
            displayedItems.add(root);
            if (root.isExpanded) {
                addChildrenRecursively(root, displayedItems, 1);
            }
        }
        notifyDataSetChanged();
    }

    private void addChildrenRecursively(Folder parent, List<Item> list, int level) {
        if (parent.children == null) return;
        for (Item child : parent.children) {
            child.setLevel(level);
            list.add(child);
            if (child instanceof Folder && ((Folder) child).isExpanded) {
                addChildrenRecursively((Folder) child, list, level + 1);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return displayedItems.get(position) instanceof Folder ? TYPE_FOLDER : TYPE_FILE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_FOLDER) {
            View view = inflater.inflate(R.layout.item_folder, parent, false);
            return new FolderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_file, parent, false);
            return new FileViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = displayedItems.get(position);
        int paddingLeft = item.getLevel() * 50;
        holder.itemView.setPadding(paddingLeft, holder.itemView.getPaddingTop(), holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());

        if (holder.getItemViewType() == TYPE_FOLDER) {
            ((FolderViewHolder) holder).bind((Folder) item, this::onFolderClicked);
        } else {
            ((FileViewHolder) holder).bind((File) item, fileClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return displayedItems.size();
    }

    private void onFolderClicked(int folderPosition) {
        if (folderPosition < 0 || folderPosition >= displayedItems.size()) return;
        Item item = displayedItems.get(folderPosition);
        if (!(item instanceof Folder)) return;

        Folder folder = (Folder) item;
        if (folder.children == null || folder.children.isEmpty()) return;

        folder.isExpanded = !folder.isExpanded;
        notifyItemChanged(folderPosition);

        if (!folder.isExpanded) {
            List<Item> itemsToRemove = new ArrayList<>();
            int startPosition = folderPosition + 1;
            if (startPosition < displayedItems.size()) {
                for (int i = startPosition; i < displayedItems.size(); i++) {
                    Item currentItem = displayedItems.get(i);
                    if (currentItem.getLevel() > folder.getLevel()) {
                        itemsToRemove.add(currentItem);
                    } else {
                        break;
                    }
                }
            }
            if (!itemsToRemove.isEmpty()) {
                displayedItems.removeAll(itemsToRemove);
                notifyItemRangeRemoved(startPosition, itemsToRemove.size());
            }
        } else {
            List<Item> childrenToInsert = new ArrayList<>();
            addChildrenRecursively(folder, childrenToInsert, folder.getLevel() + 1);
            if (!childrenToInsert.isEmpty()) {
                displayedItems.addAll(folderPosition + 1, childrenToInsert);
                notifyItemRangeInserted(folderPosition + 1, childrenToInsert.size());
            }
        }
    }

    //region Модели данных
    public interface Item extends Serializable {
        int getLevel();
        void setLevel(int level);
        List<Integer> getIndexPath();
    }

    public static class Folder implements Item {
        public final String name;
        public final List<Integer> indexPath;
        public final List<Item> children;
        public boolean isExpanded = false;
        private int level = 0;

        public Folder(String name, List<Integer> indexPath, List<Item> children) {
            this.name = name;
            this.indexPath = indexPath;
            this.children = children;
        }

        @Override
        public int getLevel() { return level; }
        @Override
        public void setLevel(int level) { this.level = level; }
        @Override
        public List<Integer> getIndexPath() { return indexPath; }
    }

    public static class File implements Item {
        public final String name;
        public final List<Integer> indexPath;
        public final String videoUrl; // Файл теперь сам знает свою ссылку
        private int level = 0;

        public File(String name, List<Integer> indexPath, String videoUrl) {
            this.name = name;
            this.indexPath = indexPath;
            this.videoUrl = videoUrl;
        }

        @Override
        public int getLevel() { return level; }
        @Override
        public void setLevel(int level) { this.level = level; }
        @Override
        public List<Integer> getIndexPath() { return indexPath; }
    }
    //endregion

    //region ViewHolders
    static class FolderViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.folder_name);
        }

        public void bind(Folder folder, FolderClickListener listener) {
            nameTextView.setText(folder.name);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onFolderClick(position);
                }
            });
        }
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.file_name);
        }

        public void bind(File file, OnFileClickListener listener) {
            nameTextView.setText(file.name);
            itemView.setOnClickListener(v -> listener.onFileClick(file));
        }
    }
    //endregion

    //region Интерфейсы
    @FunctionalInterface
    public interface OnFileClickListener {
        void onFileClick(File file);
    }

    @FunctionalInterface
    interface FolderClickListener {
        void onFolderClick(int position);
    }
    //endregion
}
