package net.lab1024.sa.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.video.service.VideoPreviewService;
import net.lab1024.sa.video.manager.VideoPreviewManager;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Video Preview Service Implementation
 *
 * Provides real-time video streaming, snapshot, PTZ control and other preview functions
 * Follows repowiki architecture design: Service layer handles business logic and transaction management
 *
 * @author SmartAdmin Team
 * @since 2025-11-18
 */
@Slf4j
@Service
@Transactional(rollbackFor = Throwable.class)
public class VideoPreviewServiceImpl extends ServiceImpl implements VideoPreviewService {

    @Resource
    private VideoPreviewManager videoPreviewManager;

    /**
     * Get live video stream URL
     *
     * @param deviceId device ID
     * @param quality video quality (HD/SD/AUTO)
     * @return video stream URL
     */
    @Override
    public String getLiveStreamUrl(Long deviceId, String quality) {
        try {
            log.info("Get live stream URL: deviceId={}, quality={}", deviceId, quality);

            if (deviceId == null) {
                throw new IllegalArgumentException("Device ID cannot be null");
            }

            // Get device stream URL through manager
            String streamUrl = videoPreviewManager.getLiveStreamUrl(deviceId, quality);

            log.info("Live stream URL generated: deviceId={}, url={}", deviceId, streamUrl);
            return streamUrl;

        } catch (Exception e) {
            log.error("Failed to get live stream URL: deviceId={}, quality={}", deviceId, quality, e);
            return null;
        }
    }

    /**
     * Get multi-channel video stream URLs
     *
     * @param deviceIds device ID list
     * @return video stream URL mapping
     */
    @Override
    public Map<Long, String> getMultiStreamUrls(List<Long> deviceIds) {
        try {
            log.info("Get multi-channel stream URLs: deviceCount={}", deviceIds != null ? deviceIds.size() : 0);

            Map<Long, String> streamUrls = new ConcurrentHashMap<>();

            if (deviceIds != null && !deviceIds.isEmpty()) {
                for (Long deviceId : deviceIds) {
                    String url = getLiveStreamUrl(deviceId, "AUTO");
                    if (url != null) {
                        streamUrls.put(deviceId, url);
                    }
                }
            }

            log.info("Multi-channel stream URLs generated: count={}", streamUrls.size());
            return streamUrls;

        } catch (Exception e) {
            log.error("Failed to get multi-channel stream URLs", e);
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Get device real-time snapshot
     *
     * @param deviceId device ID
     * @return snapshot path
     */
    @Override
    public String getRealtimeSnapshot(Long deviceId) {
        try {
            log.info("Get real-time snapshot: deviceId={}", deviceId);

            if (deviceId == null) {
                throw new IllegalArgumentException("Device ID cannot be null");
            }

            String snapshotPath = videoPreviewManager.getRealtimeSnapshot(deviceId);

            log.info("Snapshot captured: deviceId={}, path={}", deviceId, snapshotPath);
            return snapshotPath;

        } catch (Exception e) {
            log.error("Failed to capture snapshot: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * Batch get device snapshots
     *
     * @param deviceIds device ID list
     * @return snapshot path mapping
     */
    @Override
    public Map<Long, String> getBatchSnapshots(List<Long> deviceIds) {
        try {
            log.info("Get batch snapshots: deviceCount={}", deviceIds != null ? deviceIds.size() : 0);

            Map<Long, String> snapshots = new ConcurrentHashMap<>();

            if (deviceIds != null && !deviceIds.isEmpty()) {
                for (Long deviceId : deviceIds) {
                    String snapshot = getRealtimeSnapshot(deviceId);
                    if (snapshot != null) {
                        snapshots.put(deviceId, snapshot);
                    }
                }
            }

            log.info("Batch snapshots captured: count={}", snapshots.size());
            return snapshots;

        } catch (Exception e) {
            log.error("Failed to get batch snapshots", e);
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * PTZ control
     *
     * @param deviceId device ID
     * @param command control command (UP/DOWN/LEFT/RIGHT/ZOOM_IN/ZOOM_OUT/FOCUS_NEAR/FOCUS_FAR)
     * @param speed speed (1-7)
     * @param preset preset position (optional)
     * @return control result
     */
    @Override
    public boolean ptzControl(Long deviceId, String command, Integer speed, Integer preset) {
        try {
            log.info("PTZ control: deviceId={}, command={}, speed={}, preset={}", deviceId, command, speed, preset);

            if (deviceId == null || command == null) {
                throw new IllegalArgumentException("Device ID and command cannot be null");
            }

            // Validate command
            List<String> validCommands = List.of("UP", "DOWN", "LEFT", "RIGHT", "ZOOM_IN", "ZOOM_OUT", "FOCUS_NEAR", "FOCUS_FAR");
            if (!validCommands.contains(command.toUpperCase())) {
                throw new IllegalArgumentException("Invalid PTZ command: " + command);
            }

            // Validate speed if provided
            if (speed != null && (speed < 1 || speed > 7)) {
                throw new IllegalArgumentException("Speed must be between 1 and 7");
            }

            boolean result = videoPreviewManager.ptzControl(deviceId, command, speed, preset);

            log.info("PTZ control completed: deviceId={}, result={}", deviceId, result);
            return result;

        } catch (Exception e) {
            log.error("PTZ control failed: deviceId={}, command={}", deviceId, command, e);
            return false;
        }
    }

    /**
     * Set preset position
     *
     * @param deviceId device ID
     * @param preset preset number
     * @param name preset name
     * @return setting result
     */
    @Override
    public boolean setPreset(Long deviceId, Integer preset, String name) {
        try {
            log.info("Set preset: deviceId={}, preset={}, name={}", deviceId, preset, name);

            if (deviceId == null || preset == null) {
                throw new IllegalArgumentException("Device ID and preset cannot be null");
            }

            boolean result = videoPreviewManager.setPreset(deviceId, preset, name);

            log.info("Preset set completed: deviceId={}, preset={}, result={}", deviceId, preset, result);
            return result;

        } catch (Exception e) {
            log.error("Failed to set preset: deviceId={}, preset={}", deviceId, preset, e);
            return false;
        }
    }

    /**
     * Call preset position
     *
     * @param deviceId device ID
     * @param preset preset number
     * @return call result
     */
    @Override
    public boolean callPreset(Long deviceId, Integer preset) {
        try {
            log.info("Call preset: deviceId={}, preset={}", deviceId, preset);

            if (deviceId == null || preset == null) {
                throw new IllegalArgumentException("Device ID and preset cannot be null");
            }

            boolean result = videoPreviewManager.callPreset(deviceId, preset);

            log.info("Preset call completed: deviceId={}, preset={}, result={}", deviceId, preset, result);
            return result;

        } catch (Exception e) {
            log.error("Failed to call preset: deviceId={}, preset={}", deviceId, preset, e);
            return false;
        }
    }

    /**
     * Get device preview status
     *
     * @param deviceId device ID
     * @return device preview status information
     */
    @Override
    public Object getPreviewStatus(Long deviceId) {
        try {
            log.info("Get preview status: deviceId={}", deviceId);

            if (deviceId == null) {
                throw new IllegalArgumentException("Device ID cannot be null");
            }

            Object status = videoPreviewManager.getPreviewStatus(deviceId);

            log.info("Preview status retrieved: deviceId={}", deviceId);
            return status;

        } catch (Exception e) {
            log.error("Failed to get preview status: deviceId={}", deviceId, e);
            return null;
        }
    }

    /**
     * Start video preview session
     *
     * @param deviceId device ID
     * @param userId user ID
     * @return session ID
     */
    @Override
    public String startPreviewSession(Long deviceId, Long userId) {
        try {
            log.info("Start preview session: deviceId={}, userId={}", deviceId, userId);

            if (deviceId == null || userId == null) {
                throw new IllegalArgumentException("Device ID and user ID cannot be null");
            }

            String sessionId = videoPreviewManager.startPreviewSession(deviceId, userId);

            log.info("Preview session started: deviceId={}, userId={}, sessionId={}", deviceId, userId, sessionId);
            return sessionId;

        } catch (Exception e) {
            log.error("Failed to start preview session: deviceId={}, userId={}", deviceId, userId, e);
            return null;
        }
    }

    /**
     * Stop video preview session
     *
     * @param sessionId session ID
     * @return stop result
     */
    @Override
    public boolean stopPreviewSession(String sessionId) {
        try {
            log.info("Stop preview session: sessionId={}", sessionId);

            if (sessionId == null) {
                throw new IllegalArgumentException("Session ID cannot be null");
            }

            boolean result = videoPreviewManager.stopPreviewSession(sessionId);

            log.info("Preview session stopped: sessionId={}, result={}", sessionId, result);
            return result;

        } catch (Exception e) {
            log.error("Failed to stop preview session: sessionId={}", sessionId, e);
            return false;
        }
    }

    /**
     * Get current preview session list
     *
     * @param userId user ID
     * @return session list
     */
    @Override
    public List<Object> getActivePreviewSessions(Long userId) {
        try {
            log.info("Get active preview sessions: userId={}", userId);

            List<Object> userSessions = videoPreviewManager.getActivePreviewSessions(userId)
                .stream()
                .map(session -> (Object) session)
                .toList();

            log.info("Active preview sessions retrieved: userId={}, count={}", userId, userSessions.size());
            return userSessions;

        } catch (Exception e) {
            log.error("Failed to get active preview sessions: userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * Video stream quality switch
     *
     * @param deviceId device ID
     * @param quality target quality
     * @return switch result
     */
    @Override
    public boolean switchStreamQuality(Long deviceId, String quality) {
        try {
            log.info("Switch stream quality: deviceId={}, quality={}", deviceId, quality);

            if (deviceId == null || quality == null) {
                throw new IllegalArgumentException("Device ID and quality cannot be null");
            }

            boolean result = videoPreviewManager.switchStreamQuality(deviceId, quality);

            log.info("Stream quality switched: deviceId={}, quality={}, result={}", deviceId, quality, result);
            return result;

        } catch (Exception e) {
            log.error("Failed to switch stream quality: deviceId={}, quality={}", deviceId, quality, e);
            return false;
        }
    }

    /**
     * Audio control
     *
     * @param deviceId device ID
     * @param enabled whether to enable audio
     * @return control result
     */
    @Override
    public boolean controlAudio(Long deviceId, boolean enabled) {
        try {
            log.info("Control audio: deviceId={}, enabled={}", deviceId, enabled);

            if (deviceId == null) {
                throw new IllegalArgumentException("Device ID cannot be null");
            }

            boolean result = videoPreviewManager.controlAudio(deviceId, enabled);

            log.info("Audio control completed: deviceId={}, enabled={}, result={}", deviceId, enabled, result);
            return result;

        } catch (Exception e) {
            log.error("Failed to control audio: deviceId={}, enabled={}", deviceId, enabled, e);
            return false;
        }
    }

    /**
     * Recording control
     *
     * @param deviceId device ID
     * @param enabled whether to enable recording
     * @return control result
     */
    @Override
    public boolean controlRecording(Long deviceId, boolean enabled) {
        try {
            log.info("Control recording: deviceId={}, enabled={}", deviceId, enabled);

            if (deviceId == null) {
                throw new IllegalArgumentException("Device ID cannot be null");
            }

            boolean result = videoPreviewManager.controlRecording(deviceId, enabled);

            log.info("Recording control completed: deviceId={}, enabled={}, result={}", deviceId, enabled, result);
            return result;

        } catch (Exception e) {
            log.error("Failed to control recording: deviceId={}, enabled={}", deviceId, enabled, e);
            return false;
        }
    }

    /**
     * Get device capabilities information
     *
     * @param deviceId device ID
     * @return device capabilities information
     */
    @Override
    public Object getDeviceCapabilities(Long deviceId) {
        try {
            log.info("Get device capabilities: deviceId={}", deviceId);

            if (deviceId == null) {
                throw new IllegalArgumentException("Device ID cannot be null");
            }

            Object capabilities = videoPreviewManager.getDeviceCapabilities(deviceId);

            log.info("Device capabilities retrieved: deviceId={}", deviceId);
            return capabilities;

        } catch (Exception e) {
            log.error("Failed to get device capabilities: deviceId={}", deviceId, e);
            return null;
        }
    }
}