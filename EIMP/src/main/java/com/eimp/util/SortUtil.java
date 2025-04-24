package com.eimp.util;

import com.eimp.component.ThumbnailPanel;

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
}
