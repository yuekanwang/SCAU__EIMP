package com.eimp.util;

import com.eimp.component.ThumbnailPanel;
import javafx.collections.ObservableList;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SortUtil {
    private static final Collator COLLATOR_INSTANCE = Collator.getInstance(Locale.CHINA);

    private SortUtil() {
    }

    public static void sortThumbnailPanel(List<ThumbnailPanel> thumbnailPanels, String sortOrder) {
        switch (sortOrder) {
            case SortOrder.ASC_SORT_BY_NAME ->
                    thumbnailPanels.sort(((o1, o2) -> COLLATOR_INSTANCE.compare(o1.getImageUtil().getFileName(), o2.getImageUtil().getFileName())));
            case SortOrder.DESC_SORT_BY_NAME ->
                    thumbnailPanels.sort(((o1, o2) -> COLLATOR_INSTANCE.compare(o2.getImageUtil().getFileName(), o1.getImageUtil().getFileName())));
            case SortOrder.ASC_SORT_BY_TIME ->
                    thumbnailPanels.sort(Comparator.comparing(o -> o.getImageUtil().getCreationTime()));
            case SortOrder.DESC_SORT_BY_TIME ->
                    thumbnailPanels.sort((o1, o2) -> o2.getImageUtil().getCreationTime().compareTo(o1.getImageUtil().getCreationTime()));
            case SortOrder.ASC_SORT_BY_SIZE ->
                    thumbnailPanels.sort(Comparator.comparingLong(o -> o.getImageUtil().getSizeOfBytes()));
            case SortOrder.DESC_SORT_BY_SIZE ->
                    thumbnailPanels.sort((o1, o2) -> Long.compare(o2.getImageUtil().getSizeOfBytes(), o1.getImageUtil().getSizeOfBytes()));
        }
    }

    /**
     * 图像列表排序
     * @param imageFiles 图像列表
     * @param sortOrder 排序规则
     */
    public static void sortImageFile(ObservableList<ImageUtil> imageFiles, String sortOrder) {
        switch (sortOrder) {
            case SortOrder.ASC_SORT_BY_NAME ->
                    imageFiles.sort((o1, o2) -> COLLATOR_INSTANCE.compare(o1.getFileName(), o2.getFileName()));
            case SortOrder.DESC_SORT_BY_NAME ->
                    imageFiles.sort((o1, o2) -> COLLATOR_INSTANCE.compare(o2.getFileName(), o1.getFileName()));
            case SortOrder.ASC_SORT_BY_TIME -> imageFiles.sort(Comparator.comparing(ImageUtil::getCreationTime));
            case SortOrder.DESC_SORT_BY_TIME ->
                    imageFiles.sort((o1, o2) -> o2.getCreationTime().compareTo(o1.getCreationTime()));
            case SortOrder.ASC_SORT_BY_SIZE -> imageFiles.sort(Comparator.comparingLong(ImageUtil::getSizeOfBytes));
            case SortOrder.DESC_SORT_BY_SIZE ->
                    imageFiles.sort((o1, o2) -> Long.compare(o2.getSizeOfBytes(), o1.getSizeOfBytes()));
        }
    }
}
