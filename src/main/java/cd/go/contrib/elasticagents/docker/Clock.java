package cd.go.contrib.elasticagents.docker;

import org.joda.time.DateTime;
import org.joda.time.Period;

public interface Clock {
    DateTime now();

    Clock DEFAULT = new Clock() {
        @Override
        public DateTime now() {
            return new DateTime();
        }
    };

    class TestClock implements Clock {

        DateTime time = null;

        public TestClock(DateTime time) {
            this.time = time;
        }

        public TestClock() {
            this(new DateTime());
        }

        @Override
        public DateTime now() {
            return time;
        }

        public TestClock reset() {
            time = new DateTime();
            return this;
        }

        public TestClock set(DateTime time) {
            this.time = time;
            return this;
        }

        public TestClock rewind(Period period) {
            this.time = this.time.minus(period);
            return this;
        }

        public TestClock forward(Period period) {
            this.time = this.time.plus(period);
            return this;
        }
    }
}
