package eu.sshopencloud.marketplace.conf.converters;

import eu.sshopencloud.marketplace.model.auth.UserStatus;


public class UserStatusFormatter extends BaseEnumFormatter<UserStatus> {

    private static final String USER_STATUS_NAME = "user-status";

    @Override
    protected UserStatus toEnum(String enumValue) {
        return UserStatus.valueOf(enumValue);
    }

    @Override
    protected String getEnumName() {
        return USER_STATUS_NAME;
    }

}
