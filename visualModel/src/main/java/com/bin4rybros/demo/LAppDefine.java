/*
 * Copyright(c) Live2D Inc. All rights reserved.
 *
 * Use of this source code is governed by the Live2D Open Software license
 * that can be found at http://live2d.com/eula/live2d-open-software-license-agreement_en.html.
 */

package com.bin4rybros.demo;

import com.bin4rybros.sdk.cubism.framework.CubismFrameworkConfig.LogLevel;

/**
 * Constants used in this sample app.
 */
public class LAppDefine {
    /**
     * Scaling rate.
     */
    public enum Scale {
        /**
         * Default scaling rate
         */
        DEFAULT(1.0f),
        /**
         * Maximum scaling rate
         */
        MAX(2.0f),
        /**
         * Minimum scaling rate
         */
        MIN(0.8f);

        private final float value;

        Scale(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
    }

    /**
     * Logical view coordinate system.
     */
    public enum LogicalView {
        /**
         * Left end
         */
        LEFT(-1.0f),
        /**
         * Right end
         */
        RIGHT(1.0f),
        /**
         * Bottom end
         */
        BOTTOM(-1.0f),
        /**
         * Top end
         */
        TOP(1.0f);

        private final float value;

        LogicalView(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
    }

    /**
     * Maximum logical view coordinate system.
     */
    public enum MaxLogicalView {
        /**
         * Maximum left end
         */
        LEFT(-2.0f),
        /**
         * Maximum right end
         */
        RIGHT(2.0f),
        /**
         * Maximum bottom end
         */
        BOTTOM(-2.0f),
        /**
         * Maximum top end
         */
        TOP(2.0f);

        private final float value;

        MaxLogicalView(float value) {
            this.value = value;
        }

        public float getValue() {
            return value;
        }
    }

    /**
     * Path of image materials.
     */
    public enum ResourcePath {
        /**
         * Relative path of the material directory
         */
        ROOT(""),
        /**
         * Background image file
         */
        BACK_IMAGE("violet_bg.png"),
        /**
         * Gear image file
         */
        GEAR_IMAGE("icon_gear.png");
//        /**
//         * Power button image file
//         */
//        POWER_IMAGE("close.png");

        private final String path;

        ResourcePath(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    /**
     * Model directory name.
     */
    public enum ModelDir {
        KAIA(0, "kaia"),
        KAIA2(1, "kaia2"),
        KAIA3(2, "kaia3"),
        KAIA4(3, "kaia4");
//        KAIA_SHORTSLEEVE(2, "kaiaShortsleeve"),
//        KAIA_SHORTSLEEVE_NOCOLLAR(3, "kaiaShortsleeveNoCollar");
//        HARU(1, "Haru");
//        HIYORI(2, "Hiyori"),
//        MARK(3, "Mark"),
//        NATORI(4, "Natori"),
//        RICE(5, "Rice"),
//        MAO(6, "Mao");

        private final int order;
        private final String dirName;

        ModelDir(int order, String dirName) {
            this.order = order;
            this.dirName = dirName;
        }

        public int getOrder() {
            return order;
        }

        public String getDirName() {
            return dirName;
        }

    }

    /**
     * Motion group
     */
    public enum MotionGroup {
        /**
         * ID of the motion to be played at idling.
         */
        IDLE("Idle"),
        /**
         * ID of the motion to be played at tapping body.
         */
        TAP_BODY("TapBody"),
        TAP_HEAD("TapHead"),
        AFFIRMATION("Affirmation"),
        ASKING("Asking"),
        NEGATIVE("Negative"),
        SPEAKING("Speaking"),
        SWITCH("Switch"),
        SLEEP("Sleep"),
        THINKING("Thinking");

        private final String id;

        MotionGroup(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * [Head] tag for hit detection.
     * (Match with external definition file(json))
     */
    public enum HitAreaName {
        HEAD("Head"),
        BODY("Body"),
        DRONE("Drone"),
        CLIPBOARD("Clipboard"),
        LEGS("Legs");

        private final String id;

        HitAreaName(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    /**
     * Motion priority
     */
    public enum Priority {
        NONE(0),
        IDLE(1),
        NORMAL(2),
        FORCE(3);

        private final int priority;

        Priority(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    /**
     * MOC3の整合性を検証するかどうか。有効ならtrue。
     */
    public static final boolean MOC_CONSISTENCY_VALIDATION_ENABLE = true;

    /**
     * Enable/Disable debug logging.
     */
    public static final boolean DEBUG_LOG_ENABLE = true;
    /**
     * Enable/Disable debug logging for processing tapping information.
     */
    public static final boolean DEBUG_TOUCH_LOG_ENABLE = true;
    /**
     * Setting the level of the log output from the Framework.
     */
    public static final LogLevel cubismLoggingLevel = LogLevel.VERBOSE;
    /**
     * Enable/Disable premultiplied alpha.
     */
    public static final boolean PREMULTIPLIED_ALPHA_ENABLE = true;

    /**
     * Flag whether to draw to the target held by LAppView. (If both USE_RENDER_TARGET and USE_MODEL_RENDER_TARGET are true, this variable is given priority over USE_MODEL_RENDER_TARGET.)
     */
    public static final boolean USE_RENDER_TARGET = false;
    /**
     * Flag whether to draw to the target that each LAppModel has.
     */
    public static final boolean USE_MODEL_RENDER_TARGET = false;
}
