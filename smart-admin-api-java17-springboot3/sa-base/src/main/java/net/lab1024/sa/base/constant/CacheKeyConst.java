package net.lab1024.sa.base.constant;

/**
 * 缓存key常量
 *
 * @Author 1024创新实验室: 罗伊
 * @Date 2022-05-30 21:22:12
 * @Wechat zhuoda1024
 * @Email lab1024@163.com
 * @Copyright <a href="https://1024lab.net">1024创新实验室</a>
 */
public class CacheKeyConst {

    public static class Dict {

        public static final String DICT_DATA = "dict_data_cache";

    }

    public static class Visitor {

        /**
         * 访客二维码缓存
         */
        public static final String VISITOR_QR_CODE = "visitor:qr:code:";

        /**
         * 访客二维码反向索引
         */
        public static final String VISITOR_QR_REVERSE = "visitor:qr:reverse:";

        /**
         * 访客预约信息缓存
         */
        public static final String VISITOR_RESERVATION = "visitor:reservation:";

        /**
         * 访客统计数据缓存
         */
        public static final String VISITOR_STATISTICS = "visitor:statistics:";

        /**
         * 访客黑名单缓存
         */
        public static final String VISITOR_BLACKLIST = "visitor:blacklist:";

        /**
         * 访客访问次数缓存
         */
        public static final String VISITOR_COUNT = "visitor:count:";

    }

}