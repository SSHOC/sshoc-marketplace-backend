package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.dto.auth.UserOrder;

public class UserOrderFormatter extends BaseEnumFormatter<UserOrder> {

    private static final String USER_ORDER_NAME = "user-order";

    @Override
    protected UserOrder toEnum(String enumValue) {
        return UserOrder.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return USER_ORDER_NAME;
    }

}
