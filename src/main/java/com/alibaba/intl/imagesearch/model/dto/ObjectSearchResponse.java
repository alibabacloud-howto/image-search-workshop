package com.alibaba.intl.imagesearch.model.dto;

import java.util.List;

/**
 * Result of a search by similar image.
 *
 * @author Alibaba Cloud
 */
public class ObjectSearchResponse {

    private List<AugmentedAuction> auctions;
    private String rawImageSearchResponseJson;
    private ImageRegion objectRegion;

    public ObjectSearchResponse() {
    }

    public ObjectSearchResponse(List<AugmentedAuction> auctions, String rawImageSearchResponseJson, ImageRegion objectRegion) {
        this.auctions = auctions;
        this.rawImageSearchResponseJson = rawImageSearchResponseJson;
        this.objectRegion = objectRegion;
    }

    public List<AugmentedAuction> getAuctions() {
        return auctions;
    }

    public void setAuctions(List<AugmentedAuction> auctions) {
        this.auctions = auctions;
    }

    public String getRawImageSearchResponseJson() {
        return rawImageSearchResponseJson;
    }

    public void setRawImageSearchResponseJson(String rawImageSearchResponseJson) {
        this.rawImageSearchResponseJson = rawImageSearchResponseJson;
    }

    public ImageRegion getObjectRegion() {
        return objectRegion;
    }

    public void setObjectRegion(ImageRegion objectRegion) {
        this.objectRegion = objectRegion;
    }

    @Override
    public String toString() {
        return "ObjectSearchResponse{" +
                "auctions=" + auctions +
                ", rawImageSearchResponseJson='" + rawImageSearchResponseJson + '\'' +
                ", objectRegion=" + objectRegion +
                '}';
    }
}
