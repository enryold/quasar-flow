package it.enryold.quasarflow.models.utils;


import java.util.Date;

public class QMetric {

    private String flowName;
    private String componentName;
    private String metricName;
    private long creation;

    private QMetric(Builder builder) {
        flowName = builder.flowName;
        componentName = builder.componentName;
        metricName = builder.metricName;
        creation = new Date().getTime();
    }

    public String getFlowName() {
        return flowName;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getMetricName() {
        return metricName;
    }

    public long getCreation() {
        return creation;
    }

    public static Builder Builder() {
        return new Builder();
    }


    public static final class Builder {
        private String flowName;
        private String componentName;
        private String metricName;

        private Builder() {
        }

        public Builder withFlowName(String val) {
            flowName = val;
            return this;
        }

        public Builder withComponentName(String val) {
            componentName = val;
            return this;
        }

        public Builder withMetricName(String val) {
            metricName = val;
            return this;
        }

        public QMetric build() {
            return new QMetric(this);
        }
    }

    @Override
    public String toString() {
        return "Metric of: "+componentName+"(flow:"+flowName+") with name:"+metricName+" created at:"+creation;
    }
}
