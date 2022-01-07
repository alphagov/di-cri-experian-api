package uk.gov.di.ipv.cri.experian.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrossCoreApiRequest {
    @JsonProperty private Header header;
    @JsonProperty private Payload payload;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
