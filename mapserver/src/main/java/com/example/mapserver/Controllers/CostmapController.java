package com.example.mapserver.Controllers;

import com.example.mapserver.Models.MapRecord;
import com.example.mapserver.Repositories.MapRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/costmap")
public class CostmapController {

    private static final Logger logger = LoggerFactory.getLogger(CostmapController.class);

    @Autowired
    private MapRecordRepository mapRecordRepository;

    private static final int MAX_RECORDS = 10;

    @PostMapping("/upload_map")
    public ResponseEntity<?> uploadMap(@RequestBody Map<String, Object> mapData) {
        logger.info("Map upload request received");
        try {
            long timestamp = System.currentTimeMillis();
            logger.debug("Processing map upload - Timestamp: {}", timestamp);

            // Extract map info and origin
            Map<String, Object> mapInfo = (Map<String, Object>) ((Map<String, Object>) mapData.get("map")).get("info");
            Map<String, Object> mapOrigin = (Map<String, Object>) mapInfo.get("origin");
            // System.out.println("MAP_ORIGIN_X ::: "+(double) mapOrigin.get("x"));
            List<Integer> mapDataList = (List<Integer>) ((Map<String, Object>) mapData.get("map")).get("data");

            // Extract global costmap info and origin
            Map<String, Object> globalInfo = (Map<String, Object>) ((Map<String, Object>) mapData.get("global_costmap"))
                    .get("info");
            Map<String, Object> globalOrigin = (Map<String, Object>) globalInfo.get("origin");
            List<Integer> globalDataList = (List<Integer>) ((Map<String, Object>) mapData.get("global_costmap"))
                    .get("data");

            // Extract local costmap info and origin
            Map<String, Object> localInfo = (Map<String, Object>) ((Map<String, Object>) mapData.get("local_costmap"))
                    .get("info");
            Map<String, Object> localOrigin = (Map<String, Object>) localInfo.get("origin");
            List<Integer> localDataList = (List<Integer>) ((Map<String, Object>) mapData.get("local_costmap"))
                    .get("data");

            // Extract transform data
            Map<String, Object> transform = (Map<String, Object>) mapData.get("transform");
            Map<String, Object> translation = (Map<String, Object>) transform.get("translation");
            Map<String, Object> rotation = (Map<String, Object>) transform.get("rotation");

            double mapOriginX = (double) mapOrigin.get("x");
            double mapOriginY = (double) mapOrigin.get("y");
            double mapOriginZ = (double) mapOrigin.get("z");

            // System.out.println("MapOriginX ::: "+mapOriginX);
            // System.out.println("MapOriginY ::: "+mapOriginY);
            // System.out.println("MapOriginZ ::: "+mapOriginZ);

            double globalMapOriginX = (double) globalOrigin.get("x");
            double globalMapOriginY = (double) globalOrigin.get("y");
            double globalMapOriginZ = (double) globalOrigin.get("z");

            double localMapOriginX = (double) localOrigin.get("x");
            double localMapOriginY = (double) localOrigin.get("y");
            double localMapOriginZ = (double) localOrigin.get("z");

            // Create a new MapRecord with origin data
            MapRecord newRecord = new MapRecord(
                    timestamp,
                    (int) mapInfo.get("width"), (int) mapInfo.get("height"), (double) mapInfo.get("resolution"),
                    mapDataList,
                    mapOriginX, mapOriginY, mapOriginZ,
                    (int) globalInfo.get("width"), (int) globalInfo.get("height"),
                    (double) globalInfo.get("resolution"), globalDataList,
                    globalMapOriginX, globalMapOriginY, globalMapOriginZ,
                    (int) localInfo.get("width"), (int) localInfo.get("height"), (double) localInfo.get("resolution"),
                    localDataList,
                    localMapOriginX, localMapOriginY, localMapOriginZ,
                    (double) translation.get("x"), (double) translation.get("y"), (double) translation.get("z"),
                    (double) rotation.get("x"), (double) rotation.get("y"), (double) rotation.get("z"),
                    (double) rotation.get("w"));

            // Save the new record and clean up old records
            logger.debug("Saving map record to repository");
            mapRecordRepository.save(newRecord);
            logger.info("Map record saved successfully - Timestamp: {}", timestamp);
            
            cleanOldRecords();

            logger.info("Map uploaded successfully - Timestamp: {}", timestamp);
            return ResponseEntity
                    .ok(Map.of("message", "Maps and transform uploaded successfully", "timestamp", timestamp));
        } catch (Exception e) {
            logger.error("Error uploading map", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to save maps and transform"));
        }
    }

    @GetMapping("/download_map")
    public ResponseEntity<?> downloadMap() {
        logger.debug("Map download request received");
        try {
            List<MapRecord> records = mapRecordRepository.findAllByOrderByTimestampAsc();
            if (records.isEmpty()) {
                logger.warn("No map records found");
                return ResponseEntity.ok(Map.of("available", false));
            }

            MapRecord latestRecord = records.get(records.size() - 1);
            logger.info("Map download successful - Timestamp: {}, Total records: {}", 
                    latestRecord.getTimestamp(), records.size());

            // Build the response including origin data
            Map<String, Object> response = Map.of(
                    "available", true,
                    "map", Map.of(
                            "info", latestRecord.getMapInfo(),
                            "data", latestRecord.getMapData()),
                    "global_costmap", Map.of(
                            "info", latestRecord.getGlobalCostmapInfo(),
                            "data", latestRecord.getGlobalCostmapData()),
                    "local_costmap", Map.of(
                            "info", latestRecord.getLocalCostmapInfo(),
                            "data", latestRecord.getLocalCostmapData()),
                    "transform", latestRecord.getTransformData());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error downloading map", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to download map"));
        }
    }

    private void cleanOldRecords() {
        logger.debug("Cleaning old map records");
        try {
            List<MapRecord> records = mapRecordRepository.findAllByOrderByTimestampAsc();
            int deletedCount = 0;
            while (records.size() > MAX_RECORDS) {
                mapRecordRepository.delete(records.get(0));
                records.remove(0);
                deletedCount++;
            }
            if (deletedCount > 0) {
                logger.info("Cleaned {} old map record(s)", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error cleaning old records", e);
        }
    }
}