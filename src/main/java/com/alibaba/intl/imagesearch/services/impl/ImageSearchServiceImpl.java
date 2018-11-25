/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.intl.imagesearch.services.impl;

import com.alibaba.intl.imagesearch.exceptions.InvalidConfigurationException;
import com.alibaba.intl.imagesearch.model.Configuration;
import com.alibaba.intl.imagesearch.model.ObjectCategory;
import com.alibaba.intl.imagesearch.model.ObjectImageType;
import com.alibaba.intl.imagesearch.model.dto.ImageRegion;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchAuction;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchResponse;
import com.alibaba.intl.imagesearch.model.dto.ImageStoreType;
import com.alibaba.intl.imagesearch.services.ConfigurationService;
import com.alibaba.intl.imagesearch.services.ImageSearchService;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.imagesearch.model.v20180611.*;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ImageSearchService}.
 *
 * @author Alibaba Cloud
 */
@Service
public class ImageSearchServiceImpl implements ImageSearchService {

    private static final String ERROR_MESSAGE_PREFIX = "Unable to search items from the Image Search API: ";
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageSearchServiceImpl.class);
    private static final String PRIMARY_IMG = "primaryImg";

    @Value("${httpClient.defaultConnectTimeout}")
    private String defaultConnectTimeout;

    @Value("${httpClient.defaultReadTimeout}")
    private String defaultReadTimeout;

    @Value("classpath:samples/2a5ddd8f-69fb-434c-b285-27ab57ea555d.jpg")
    private Resource imageForConfigurationCheckResource;

    private final ConfigurationService configurationService;
    private byte[] imageForConfigurationCheckData;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageSearchServiceImpl(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @PostConstruct
    public void loadImageForConfiguration() throws IOException {
        try (InputStream inputStream = imageForConfigurationCheckResource.getInputStream()) {
            imageForConfigurationCheckData = IOUtils.toByteArray(inputStream);
        }
    }

    @Override
    public void register(byte[] imageData, ObjectImageType imageType, ObjectCategory category, String uuid) {
        Configuration configuration = configurationService.load();

        // Prepare the request
        AddItemRequest request = new AddItemRequest();
        request.setInstanceName(configuration.getImageSearchInstanceName());
        request.setCatId(category.getId());
        request.setItemId(uuid);
        if (configuration.getImageSearchNamespace() != null) {
            request.setStrAttr(configuration.getImageSearchNamespace()); // Allow us to re-use the same Image Search instance for several environments
        }
        request.setCustContent("{\"dbStore\": true}");
        request.addPicture(uuid + "." + imageType.getExtension(), imageData);
        if (!request.buildPostContent()) {
            throw new IllegalStateException("Unable to build the request to add an item to the Image Search API.");
        }

        // Send the request
        IAcsClient isClient = getImageSearchClient(configuration);
        AddItemResponse response;
        try {
            response = isClient.getAcsResponse(request);
        } catch (ClientException e) {
            throw new IllegalStateException("Unable to add a new item to the Image Search API: " + e.getMessage(), e);
        }

        // Check the result
        if (response.getSuccess() == null || !response.getSuccess()) {
            throw new IllegalStateException("Unable to add a new item to the Image Search API: " +
                    " request ID = " + response.getRequestId() +
                    ", code = " + response.getCode() +
                    ", message = " + response.getMessage());
        }
    }

    @Override
    public void unregister(String uuid) {
        Configuration configuration = configurationService.load();

        DeleteItemRequest request = new DeleteItemRequest();
        request.setInstanceName(configuration.getImageSearchInstanceName());
        request.setItemId(uuid);

        if (!request.buildPostContent()) {
            throw new IllegalStateException("Unable to build the request to delete an item from the Image Search API.");
        }

        IAcsClient isClient = getImageSearchClient(configuration);
        DeleteItemResponse response;
        try {
            response = isClient.getAcsResponse(request);
        } catch (ClientException e) {
            throw new IllegalStateException("Unable to delete an item from the Image Search API: " + e.getMessage(), e);
        }

        // Check the result
        if (response.getSuccess() == null || !response.getSuccess()) {
            throw new IllegalStateException("Unable to delete an item from the Image Search API: " +
                    " request ID = " + response.getRequestId() +
                    ", code = " + response.getCode() +
                    ", message = " + response.getMessage());
        }
    }

    @Override
    public ImageSearchResponse findAllBySimilarImage(byte[] imageData, ImageRegion objectRegion) {
        Configuration configuration = configurationService.load();

        SearchItemRequest request = new SearchItemRequest();
        request.setInstanceName(configuration.getImageSearchInstanceName());
        request.setStart(0);
        request.setNum(20);
        request.setSearchPicture(imageData);
//        request.setCatId(ObjectCategory.FURNITURE.getId()); // TODO Allow the user to choose a category if he wants

        if (configuration.getImageSearchNamespace() != null) {
            request.setFilterClause("str_attr=\"" + configuration.getImageSearchNamespace() + "\""); // Allow us to share the same instance for multiple environments
        }
        //If the object region is null then do crop search.
        if (objectRegion != null) {
            request.setCrop(true);
            request.setRegion(new StringBuffer().append(objectRegion.getX()).append(",")
                    .append(objectRegion.getWidth() + objectRegion.getX()).append(",").append(objectRegion.getY())
                    .append(",").append(objectRegion.getHeight() + objectRegion.getY()).toString());
        }

        if (!request.buildPostContent()) {
            throw new IllegalStateException("Unable to build the request to search items from the Image Search API.");
        }

        IAcsClient client = getImageSearchClient(configuration);
        SearchItemResponse response;
        try {
            response = client.getAcsResponse(request);
        } catch (ClientException e) {
            throw new IllegalStateException(ERROR_MESSAGE_PREFIX + e.getMessage(), e);
        }

        // Check the result
        if (response.getSuccess() == null || !response.getSuccess()) {
            throw new IllegalStateException(ERROR_MESSAGE_PREFIX +
                    " request ID = " + response.getRequestId() +
                    ", code = " + response.getCode() +
                    ", message = " + response.getMessage());
        }

        // Convert the raw result to JSON
        String rawImageSearchResponseJson;
        try {
            rawImageSearchResponseJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Unable to convert the SearchItemResponse into JSON: " + e.getMessage(), e);
            rawImageSearchResponseJson = "";
        }

        return new ImageSearchResponse(buildImageSearchAuctions(response), rawImageSearchResponseJson, obtainImageRegion(response));
    }

    private ImageRegion obtainImageRegion(SearchItemResponse response) {
        ImageRegion imageRegion = null;

        String regionStr = response.getPicInfo().getRegion();
        if (regionStr != null) {
            int[] regionNum = Arrays.stream(regionStr.split(",")).mapToInt(Integer::parseInt).toArray();
            imageRegion = new ImageRegion(
                    regionNum[0],
                    regionNum[2],
                    regionNum[1] - regionNum[0],
                    regionNum[3] - regionNum[2]);
        }
        return imageRegion;
    }

    private List<ImageSearchAuction> buildImageSearchAuctions(SearchItemResponse response) {
        return response.getAuctions().stream()
                .map(auction -> {
                    double score = 0;
                    String[] sortExprValues = auction.getSortExprValues().split(";");
                    if (sortExprValues.length > 0) {
                        score = Double.parseDouble(sortExprValues[0]);
                    }

                    Map<String, String> custContent = new HashMap<>();
                    if (auction.getCustContent() != null) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            custContent = objectMapper.readValue(auction.getCustContent(), HashMap.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    ImageStoreType type = custContent.get("dbStore") != null ? ImageStoreType.DATABASE : ImageStoreType.OSS;
                    return new ImageSearchAuction(auction.getItemId(), auction.getCatId(),
                            getPrimaryImgOfTheAuction(auction, type, custContent), type, score, custContent);
                })
                .collect(Collectors.toList());
    }

    private String getPrimaryImgOfTheAuction(SearchItemResponse.Auction auction, ImageStoreType type, Map<String, String> custContent) {
        if (type == ImageStoreType.OSS) {
            String primaryImg = custContent.get(PRIMARY_IMG);
            if (StringUtils.isNoneEmpty(primaryImg) && !primaryImg.equalsIgnoreCase(auction.getPicName())) {
                return primaryImg;
            }
        }
        return auction.getPicName();
    }

    @Override
    public void checkImageSearchConfiguration(Configuration configuration) throws InvalidConfigurationException {
        SearchItemRequest request = new SearchItemRequest();
        request.setInstanceName(configuration.getImageSearchInstanceName());
        request.setStart(0);
        request.setNum(1);
        request.setSearchPicture(imageForConfigurationCheckData);
//        request.setCatId(ObjectCategory.OTHERS.getId());
        if (configuration.getImageSearchNamespace() != null) {
            request.setFilterClause("str_attr=\"" + configuration.getImageSearchNamespace() + "\""); // Allow us to share the same instance for multiple environments
        }
        if (!request.buildPostContent()) {
            throw new InvalidConfigurationException("Unable to build the request to search items from the Image Search API.");
        }

        try {
            IAcsClient client = getImageSearchClient(configuration);
            client.getAcsResponse(request);
        } catch (ClientException | IllegalStateException e) {
            throw new InvalidConfigurationException(ERROR_MESSAGE_PREFIX + e.getMessage(), e);
        }
    }

    private IAcsClient getImageSearchClient(Configuration configuration) {
        System.setProperty("sun.net.client.defaultConnectTimeout", defaultConnectTimeout);
        System.setProperty("sun.net.client.defaultReadTimeout", defaultReadTimeout);

        IClientProfile profile = DefaultProfile.getProfile(configuration.getRegionId(), configuration.getAccessKeyId(), configuration.getAccessKeySecret());
        try {
            DefaultProfile.addEndpoint(configuration.getRegionId(), configuration.getRegionId(), "ImageSearch", configuration.getImageSearchDomain());
        } catch (ClientException e) {
            throw new IllegalStateException("Unable to initialize the Image Search client: " + e.getMessage(), e);
        }

        return new DefaultAcsClient(profile);
    }
}
