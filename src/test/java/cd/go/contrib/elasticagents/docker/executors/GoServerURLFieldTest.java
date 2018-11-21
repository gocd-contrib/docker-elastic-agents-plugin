package cd.go.contrib.elasticagents.docker.executors;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class GoServerURLFieldTest {

    private final String displayName = "Go Server Url";
    private final GoServerURLField goServerURLField = new GoServerURLField("0");

    @Test
    public void shouldCheckBlankInput() {
        String result = goServerURLField.doValidate("");

        assertThat(result, is(this.displayName + " must not be blank."));
    }

    @Test
    public void shouldCheckIfStringIsValidUrl() {
        String result = goServerURLField.doValidate("foobar");

        assertThat(result, is(this.displayName + " must be a valid URL (https://example.com:8154/go)"));
    }

    @Test
    public void shouldCheckIfSchemeIsValid() {
        String result = goServerURLField.doValidate("example.com");

        assertThat(result, is(this.displayName + " must be a valid URL (https://example.com:8154/go)"));
    }

    @Test
    public void shouldCheckIfSchemeIsHTTPS() {
        String result = goServerURLField.doValidate("http://example.com");

        assertThat(result, is(this.displayName + " must be a valid HTTPs URL (https://example.com:8154/go)"));
    }

    @Test
    public void shouldCheckForLocalhost() {
        String result = goServerURLField.doValidate("https://localhost:8154/go");

        assertThat(result, is(this.displayName + " must not be localhost, since this gets resolved on the agents"));

        result = goServerURLField.doValidate("https://127.0.0.1:8154/go");

        assertThat(result, is(this.displayName + " must not be localhost, since this gets resolved on the agents"));
    }

    @Test
    public void shouldCheckIfUrlEndsWithContextGo() {
        String result = goServerURLField.doValidate("https://example.com:8154/");
        assertThat(result, is(this.displayName + " must be a valid URL ending with '/go' (https://example.com:8154/go)"));

        result = goServerURLField.doValidate("https://example.com:8154/crimemastergogo");
        assertThat(result, is(this.displayName + " must be a valid URL ending with '/go' (https://example.com:8154/go)"));
    }

    @Test
    public void shouldReturnNullForValidUrls() {
        String result = goServerURLField.doValidate("https://example.com:8154/go");
        assertThat(result, is(nullValue()));

        result = goServerURLField.doValidate("https://example.com:8154/go/");
        assertThat(result, is(nullValue()));

        result = goServerURLField.doValidate("https://example.com:8154/foo/go/");
        assertThat(result, is(nullValue()));
    }
}
