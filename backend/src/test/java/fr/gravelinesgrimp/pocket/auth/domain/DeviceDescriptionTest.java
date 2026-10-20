package fr.gravelinesgrimp.pocket.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.gravelinesgrimp.pocket.auth.domain.DeviceDescription.Browser;
import fr.gravelinesgrimp.pocket.auth.domain.DeviceDescription.Platform;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeviceDescriptionTest {

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "Mozilla/5.0 (iPhone; CPU iPhone OS 26_0 like Mac OS X) AppleWebKit/605.1.15"
                    + " (KHTML, like Gecko) Version/26.0 Mobile/15E148 Safari/604.1|SAFARI|IPHONE",
                "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko)"
                        + " Chrome/141.0.0.0 Mobile Safari/537.36|CHROME|ANDROID",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)"
                        + " Chrome/141.0.0.0 Safari/537.36 Edg/141.0.0.0|EDGE|WINDOWS",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 15.6; rv:144.0) Gecko/20100101"
                        + " Firefox/144.0|FIREFOX|MAC",
                "Mozilla/5.0 (Linux; Android 15; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko)"
                        + " SamsungBrowser/28.0 Chrome/130.0.0.0 Mobile Safari/537.36"
                        + "|SAMSUNG_INTERNET|ANDROID",
                "Mozilla/5.0 (iPad; CPU OS 26_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like"
                        + " Gecko) CriOS/141.0 Mobile/15E148 Safari/604.1|CHROME|IPAD",
                "curl/8.9.1|OTHER|OTHER",
            })
    void recognizesCommonDevices(String userAgent, Browser browser, Platform platform) {
        assertThat(DeviceDescription.fromUserAgent(userAgent))
                .isEqualTo(new DeviceDescription(browser, platform));
    }

    @ParameterizedTest
    @CsvSource(value = {"''", "'   '"})
    void unknownWithoutUserAgent(String userAgent) {
        assertThat(DeviceDescription.fromUserAgent(userAgent)).isEqualTo(DeviceDescription.UNKNOWN);
        assertThat(DeviceDescription.fromUserAgent(null)).isEqualTo(DeviceDescription.UNKNOWN);
    }
}
