package com.alibaba.intl.imagesearch.model.dto;

import com.alibaba.intl.imagesearch.model.RecognizableObject;

import java.util.Map;

/**
 * Enrich an {@link ImageSearchAuction} with data from the database.
 *
 * @author Alibaba Cloud
 */
public class AugmentedAuction extends ImageSearchAuction {

    private RecognizableObject recognizableObject;

    public AugmentedAuction() {
    }

    public AugmentedAuction(ImageSearchAuction auction, RecognizableObject recognizableObject) {
        super(auction.getItemId(),
                auction.getCatId(),
                auction.getPicName(),
                auction.getImageStoreType(),
                auction.getSimilarityScore(),
                auction.getCustomContent());
        this.recognizableObject = recognizableObject;
    }

    public AugmentedAuction(
            String itemId,
            String catId,
            String picName,
            ImageStoreType imageStoreType,
            double similarityScore,
            Map<String, String> customContent,
            RecognizableObject recognizableObject) {
        super(itemId, catId, picName, imageStoreType, similarityScore, customContent);
        this.recognizableObject = recognizableObject;
    }

    public RecognizableObject getRecognizableObject() {
        return recognizableObject;
    }

    public void setRecognizableObject(RecognizableObject recognizableObject) {
        this.recognizableObject = recognizableObject;
    }

    @Override
    public String toString() {
        return "AugmentedAuction{" +
                "recognizableObject=" + recognizableObject +
                '}';
    }
}
