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

package com.alibaba.intl.imagesearch.facade.controllers;

import com.alibaba.intl.imagesearch.facade.dto.ObjectDTO;
import com.alibaba.intl.imagesearch.facade.dto.ObjectSearchResponseDTO;
import com.alibaba.intl.imagesearch.facade.dto.ObjectWithScoreDTO;
import com.alibaba.intl.imagesearch.facade.exceptions.InvalidImageException;
import com.alibaba.intl.imagesearch.facade.exceptions.InvalidObjectException;
import com.alibaba.intl.imagesearch.model.Configuration;
import com.alibaba.intl.imagesearch.model.ObjectCategory;
import com.alibaba.intl.imagesearch.model.ObjectImageType;
import com.alibaba.intl.imagesearch.model.RecognizableObject;
import com.alibaba.intl.imagesearch.model.dto.ImageRegion;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchAuction;
import com.alibaba.intl.imagesearch.model.dto.ImageSearchResponse;
import com.alibaba.intl.imagesearch.model.dto.ImageStoreType;
import com.alibaba.intl.imagesearch.services.ConfigurationService;
import com.alibaba.intl.imagesearch.services.RecognizableObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handle operations behind the "/objects" path.
 *
 * @author Alibaba Cloud
 */
@RestController
public class ObjectController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectController.class);
    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
    private static final String NAME_REGEX = "^[a-zA-Z0-9 \\.\\-_\\(\\)]+$";

    private final RecognizableObjectService recognizableObjectService;
    private final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);
    private final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    @Autowired
    private ConfigurationService configurationService;

    public ObjectController(RecognizableObjectService recognizableObjectService) {
        this.recognizableObjectService = recognizableObjectService;
    }

    /**
     * Create an object in the database.
     * Note: the image is automatically resized if its resolution is smaller than 200x200 pixels or
     * if its file size is bigger than 2MB.
     *
     * @return Created object.
     */
    @RequestMapping(value = "/objects", method = RequestMethod.POST)
    public ObjectDTO create(@RequestPart("json") ObjectDTO objectDTO,
                            @RequestPart("imageFile") MultipartFile imageFile,
                            @RequestPart("thumbnailFile") MultipartFile thumbnailFile)
            throws InvalidObjectException, InvalidImageException {
        LOGGER.info("Create the object (image name = '{}', image size = {}kB, thumbnail size = {}kB): {}",
                imageFile.getOriginalFilename(), imageFile.getSize() / 1024, thumbnailFile.getSize() / 1024, objectDTO);

        validateObject(objectDTO);

        RecognizableObject object = convertObjectDTOToModel(objectDTO);
        object.setImageData(readImageFile(imageFile));
        object.setThumbnailData(readImageFile(thumbnailFile));

        RecognizableObject savedObject = recognizableObjectService.create(object);
        return convertModelObjectToDTO(savedObject);
    }

    /**
     * Update an object in the database.
     * Note: the {@link ObjectDTO#uuid} and {@link ObjectDTO#imageUrl} cannot be modified.
     *
     * @return Updated object.
     */
    @RequestMapping(value = "/objects/{uuid}", method = RequestMethod.PUT)
    public ObjectDTO update(@PathVariable("uuid") String uuid, @RequestBody ObjectDTO objectDTO)
            throws InvalidObjectException {
        LOGGER.info("Update the object (uuid = {}): {}", uuid, objectDTO);

        validateObject(objectDTO);
        if (!objectDTO.getUuid().equals(uuid)) {
            throw new InvalidObjectException(
                    "The uuid from the path ('" + uuid + "') doesn't match with the object one ('" + objectDTO.getUuid() + "').");
        }

        RecognizableObject object = convertObjectDTOToModel(objectDTO);

        RecognizableObject savedObject = recognizableObjectService.update(object);
        return convertModelObjectToDTO(savedObject);
    }

    /**
     * Delete the object with the given uuid.
     *
     * @return {@link HttpStatus#OK} if the file was deleted successfully,
     * {@link HttpStatus#NOT_FOUND} if it doesn't exist.
     */
    @RequestMapping(value = "/objects/{uuid}", method = RequestMethod.DELETE)
    public HttpStatus delete(@PathVariable("uuid") String uuid) throws InvalidObjectException {
        LOGGER.info("Delete the object with the uuid: {}", uuid);

        validateUuid(uuid);

        boolean success = recognizableObjectService.delete(uuid);
        return success ? HttpStatus.OK : HttpStatus.NOT_FOUND;
    }

    /**
     * @return Object with the given uuid.
     */
    @RequestMapping(value = "/objects/{uuid}", method = RequestMethod.GET)
    public ResponseEntity<ObjectDTO> findByUuid(@PathVariable("uuid") String uuid) throws InvalidObjectException {
        LOGGER.debug("Find the object with the uuid: {}", uuid);

        validateUuid(uuid);

        RecognizableObject object = recognizableObjectService.findByUuid(uuid);
        if (object == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ObjectDTO objectDTO = convertModelObjectToDTO(object);
        return new ResponseEntity<>(objectDTO, HttpStatus.OK);
    }

    /**
     * Find all objects sorted in alpha-numeric order on their name attribute.
     *
     * @return All registered objects.
     */
    @RequestMapping(value = "/objects", method = RequestMethod.GET)
    public List<ObjectDTO> findAll() {
        LOGGER.debug("Find all objects.");

        List<RecognizableObject> objects = recognizableObjectService.findAll();
        return objects.stream()
                .map(this::convertModelObjectToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Find all objects that match the given image.
     *
     * @param imageFile    Uploaded image file.
     * @param objectRegion Updated crop image region
     * @return Result page name.
     * @throws InvalidImageException
     */
    @RequestMapping(value = "/objects/findAllBySimilarImage", method = RequestMethod.POST)
    public ObjectSearchResponseDTO findAllBySimilarImage(@RequestParam("imageFile") MultipartFile imageFile, @RequestPart(name = "objectRegion", required = false) ImageRegion objectRegion)
            throws InvalidImageException {
        LOGGER.info("Find all objects similar to the given image (name = '{}', size = {}kB).",
                imageFile.getOriginalFilename(), imageFile.getSize() / 1024);

        ImageSearchResponse imageSearchResponse = recognizableObjectService.findAllBySimilarImage(readImageFile(imageFile), objectRegion);

        List<ObjectWithScoreDTO> objectWithScores = imageSearchResponse.getImageSearchAuctions().stream()
                .map(isu -> new ObjectWithScoreDTO(convertModelObjectToDTO(isu), isu.getSimilarityScore()))
                .collect(Collectors.toList());

        return new ObjectSearchResponseDTO(objectWithScores, imageSearchResponse.getRawImageSearchResponseJson(), imageSearchResponse.getObjectRegion());
    }

    /**
     * Provide the image of the object related to the given UUID.
     *
     * @return Image associated to the object with the given uuid.
     */
    @RequestMapping(value = "/objects/{uuid}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> findObjectImageByUuid(@PathVariable("uuid") String uuid) throws InvalidObjectException {
        LOGGER.debug("Find the object image with the uuid: {}", uuid);

        validateUuid(uuid);

        RecognizableObject object = recognizableObjectService.findByUuid(uuid);
        byte[] imageData = object.getImageData();
        if (imageData == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(object.getImageType() == ObjectImageType.JPEG ? MediaType.IMAGE_JPEG : MediaType.IMAGE_PNG)
                    .body(imageData);
        }
    }

    /**
     * Provide the thumbnail of the object related to the given UUID.
     *
     * @return Thumbnail image associated to the object with the given uuid.
     */
    @RequestMapping(value = "/objects/{uuid}/thumbnail", method = RequestMethod.GET)
    public ResponseEntity<byte[]> findObjectThumbnailByUuid(@PathVariable("uuid") String uuid) throws InvalidObjectException {
        LOGGER.debug("Find the object image thumbnail with the uuid: {}", uuid);

        validateUuid(uuid);

        RecognizableObject object = recognizableObjectService.findByUuid(uuid);
        byte[] thumbnailData = object.getThumbnailData();
        if (thumbnailData == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(object.getImageType() == ObjectImageType.JPEG ? MediaType.IMAGE_JPEG : MediaType.IMAGE_PNG)
                    .body(thumbnailData);
        }
    }

    private void validateObject(ObjectDTO object) throws InvalidObjectException {
        if (object == null) {
            throw new InvalidObjectException("The object cannot be null.");
        }
        validateUuid(object.getUuid());
        if (object.getName() == null || !NAME_PATTERN.matcher(object.getName()).matches()) {
            throw new InvalidObjectException("The name is invalid. It must respect the regex /" + NAME_REGEX + "/.");
        }
        if (object.getCategory() == null) {
            String availableCategories = Arrays.stream(ObjectCategory.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new InvalidObjectException(
                    "The category is empty, it must have one of the following values: " + availableCategories + ".");
        }
        if (object.getImageType() == null) {
            String availableTypes = Arrays.stream(ObjectImageType.values())
                    .map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new InvalidObjectException(
                    "The imageType is empty, it must have one of the following values: " + availableTypes + ".");
        }
    }

    private void validateUuid(String uuid) throws InvalidObjectException {
        if (uuid == null || !UUID_PATTERN.matcher(uuid).matches()) {
            throw new InvalidObjectException("The uuid is invalid. It must respect the regex /" + UUID_REGEX + "/.");
        }
    }

    private RecognizableObject convertObjectDTOToModel(ObjectDTO objectDTO) {
        return new RecognizableObject(
                objectDTO.getUuid(),
                objectDTO.getName(),
                objectDTO.getCategory(),
                objectDTO.getImageType());
    }

    private ObjectDTO convertModelObjectToDTO(RecognizableObject object) {
        return new ObjectDTO(
                object.getUuid(),
                object.getName(),
                object.getCategory(),
                object.getImageType(),
                "/objects/" + object.getUuid() + "/image",
                "/objects/" + object.getUuid() + "/thumbnail");
    }

    private ObjectDTO convertModelObjectToDTO(ImageSearchAuction object) {
        Configuration configuration = configurationService.load();

        String imageUrl = object.getImageStoreType() == ImageStoreType.DATABASE ? "/objects/" + object.getItemId() + "/image"
                : configuration != null ? configuration.getOssBaseUrl() : "" + "/" + object.getPicName();
        String thumbnailUrl = object.getImageStoreType() == ImageStoreType.DATABASE ? "/objects/" + object.getItemId() + "/thumbnail"
                : configuration != null ? configuration.getOssBaseUrl() : "" + "/" + object.getPicName();
        String uuid = object.getImageStoreType() == ImageStoreType.DATABASE ? object.getItemId() : object.getItemId() + "_" + object.getPicName();

        return new ObjectDTO(
                uuid,
                object.getPicName(),
                ObjectCategory.findById(object.getCatId()),
                null,
                imageUrl,
                thumbnailUrl);
    }

    private byte[] readImageFile(MultipartFile imageFile) throws InvalidImageException {
        try {
            return imageFile.getBytes();
        } catch (IOException e) {
            throw new InvalidImageException("Unable to read the imageFile: " + e.getMessage(), e);
        }
    }
}
