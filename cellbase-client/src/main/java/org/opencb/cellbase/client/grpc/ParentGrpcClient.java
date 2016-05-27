package org.opencb.cellbase.client.grpc;

import io.grpc.ManagedChannel;

/**
 * Created by swaathi on 27/05/16.
 */
public class ParentGrpcClient {

    protected ManagedChannel channel;

    public ParentGrpcClient(ManagedChannel channel) {
        this.channel = channel;
    }

}

