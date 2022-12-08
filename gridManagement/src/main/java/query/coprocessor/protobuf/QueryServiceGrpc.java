package query.coprocessor.protobuf;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.6.1)",
    comments = "Source: NonPointQueryCondition.proto")
public final class QueryServiceGrpc {

  private QueryServiceGrpc() {}

  public static final String SERVICE_NAME = "QueryService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<query.coprocessor.protobuf.NonPointQueryCondition.QueryRequest,
      query.coprocessor.protobuf.NonPointQueryCondition.QueryResponse> METHOD_QUERY =
      io.grpc.MethodDescriptor.<query.coprocessor.protobuf.NonPointQueryCondition.QueryRequest, query.coprocessor.protobuf.NonPointQueryCondition.QueryResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "QueryService", "query"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              query.coprocessor.protobuf.NonPointQueryCondition.QueryRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              query.coprocessor.protobuf.NonPointQueryCondition.QueryResponse.getDefaultInstance()))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static QueryServiceStub newStub(io.grpc.Channel channel) {
    return new QueryServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static QueryServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new QueryServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static QueryServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new QueryServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class QueryServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void query(query.coprocessor.protobuf.NonPointQueryCondition.QueryRequest request,
        io.grpc.stub.StreamObserver<query.coprocessor.protobuf.NonPointQueryCondition.QueryResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_QUERY, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_QUERY,
            asyncUnaryCall(
              new MethodHandlers<
                query.coprocessor.protobuf.NonPointQueryCondition.QueryRequest,
                query.coprocessor.protobuf.NonPointQueryCondition.QueryResponse>(
                  this, METHODID_QUERY)))
          .build();
    }
  }

  /**
   */
  public static final class QueryServiceStub extends io.grpc.stub.AbstractStub<QueryServiceStub> {
    private QueryServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private QueryServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new QueryServiceStub(channel, callOptions);
    }

    /**
     */
    public void query(query.coprocessor.protobuf.NonPointQueryCondition.QueryRequest request,
        io.grpc.stub.StreamObserver<query.coprocessor.protobuf.NonPointQueryCondition.QueryResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_QUERY, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class QueryServiceBlockingStub extends io.grpc.stub.AbstractStub<QueryServiceBlockingStub> {
    private QueryServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private QueryServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new QueryServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public query.coprocessor.protobuf.NonPointQueryCondition.QueryResponse query(query.coprocessor.protobuf.NonPointQueryCondition.QueryRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_QUERY, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class QueryServiceFutureStub extends io.grpc.stub.AbstractStub<QueryServiceFutureStub> {
    private QueryServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private QueryServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected QueryServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new QueryServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<query.coprocessor.protobuf.NonPointQueryCondition.QueryResponse> query(
        query.coprocessor.protobuf.NonPointQueryCondition.QueryRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_QUERY, getCallOptions()), request);
    }
  }

  private static final int METHODID_QUERY = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final QueryServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(QueryServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_QUERY:
          serviceImpl.query((query.coprocessor.protobuf.NonPointQueryCondition.QueryRequest) request,
              (io.grpc.stub.StreamObserver<query.coprocessor.protobuf.NonPointQueryCondition.QueryResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static final class QueryServiceDescriptorSupplier implements io.grpc.protobuf.ProtoFileDescriptorSupplier {
    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return query.coprocessor.protobuf.NonPointQueryCondition.getDescriptor();
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (QueryServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new QueryServiceDescriptorSupplier())
              .addMethod(METHOD_QUERY)
              .build();
        }
      }
    }
    return result;
  }
}
