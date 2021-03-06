/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.codegen.docs;

import java.util.Map;
import software.amazon.awssdk.codegen.internal.ImmutableMapParameter;
import software.amazon.awssdk.codegen.model.intermediate.IntermediateModel;
import software.amazon.awssdk.codegen.model.intermediate.OperationModel;

/**
 * Implementations of {@link OperationDocProvider} for sync client methods. This implementation is for the typical
 * sync method (i.e. on that takes a request and returns a response object). Subclasses provide documentation for
 * specialized method overloads like simple no-arg methods.
 */
class SyncOperationDocProvider extends OperationDocProvider {

    private static final String DEFAULT_RETURN = "Result of the %s operation returned by the service.";

    private static final String REQUEST_BODY_DOCS =
            "The content to send to the service. A {@link RequestBody} can be created using one of " +
            "several factory methods for various sources of data. For example, to create a request body " +
            "from a file you can do the following. " +
            "<pre>{@code RequestBody.of(new File(\"myfile.txt\"))}</pre>" +
            "See documentation in {@link RequestBody} for additional details and which sources of data are supported. ";

    private static final String STREAM_RESPONSE_HANDLER_DOCS =
            "Functional interface for processing the streamed response content. The unmarshalled %s " +
            "and an InputStream to the response content are provided as parameters to the callback. " +
            "The callback may return a transformed type which will be the return value of this method. " +
            "See {@link software.amazon.awssdk.runtime.transform.StreamingResponseHandler} for details on " +
            "implementing this interface and for links to pre-canned implementations for common scenarios " +
            "like downloading to a file. ";

    private SyncOperationDocProvider(IntermediateModel model, OperationModel opModel) {
        super(model, opModel);
    }

    @Override
    protected String getDefaultServiceDocs() {
        return String.format("Invokes the %s operation.", opModel.getOperationName());
    }

    @Override
    protected String getInterfaceName() {
        return model.getMetadata().getSyncInterface();
    }

    @Override
    protected void applyReturns(DocumentationBuilder docBuilder) {
        if (opModel.hasStreamingOutput()) {
            docBuilder.returns("The transformed result of the StreamingResponseHandler.");
        } else {
            docBuilder.returns(DEFAULT_RETURN, opModel.getOperationName());
        }
    }

    @Override
    protected void applyParams(DocumentationBuilder docBuilder) {
        emitRequestParm(docBuilder);
        if (opModel.hasStreamingInput()) {
            docBuilder.param("requestBody", REQUEST_BODY_DOCS + getStreamingInputDocs());

        }
        if (opModel.hasStreamingOutput()) {
            docBuilder.param("streamingHandler", STREAM_RESPONSE_HANDLER_DOCS + getStreamingOutputDocs(),
                             opModel.getOutputShape().getShapeName(), getStreamingOutputDocs());
        }
    }

    @Override
    protected void applyThrows(DocumentationBuilder docBuilder) {
        docBuilder.syncThrows(getThrows());
    }

    /**
     * @return Factories to use for the {@link ClientType#SYNC} method type.
     */
    static Map<SimpleMethodOverload, Factory> syncFactories() {
        return ImmutableMapParameter.of(SimpleMethodOverload.NORMAL, SyncOperationDocProvider::new,
                                        SimpleMethodOverload.NO_ARG, SyncNoArg::new,
                                        SimpleMethodOverload.FILE, SyncFile::new,
                                        SimpleMethodOverload.INPUT_STREAM, SyncInputStream::new);
    }

    /**
     * Provider for streaming simple methods that take a file (to either upload from for streaming inputs or download to for
     * streaming outputs).
     */
    private static class SyncFile extends SyncOperationDocProvider {

        private SyncFile(IntermediateModel model, OperationModel opModel) {
            super(model, opModel);
        }

        @Override
        protected void applyParams(DocumentationBuilder docBuilder) {
            emitRequestParm(docBuilder);
            if (opModel.hasStreamingInput()) {
                docBuilder.param("path", SIMPLE_FILE_INPUT_DOCS + getStreamingInputDocs())
                          // Link to non-simple method for discoverability
                          .see("#%s(%s, RequestBody)", opModel.getMethodName(), opModel.getInput().getVariableType());
            }
            if (opModel.hasStreamingOutput()) {
                docBuilder.param("path", SIMPLE_FILE_OUTPUT_DOCS + getStreamingOutputDocs())
                          // Link to non-simple method for discoverability
                          .see("#%s(%s, StreamingResponseHandler)", opModel.getMethodName(),
                               opModel.getInput().getVariableType());
            }
        }
    }

    /**
     * Provider for streaming output simple methods that return an {@link software.amazon.awssdk.sync.ResponseInputStream}
     * containing response content and unmarshalled POJO. Only applicable to operations that have a streaming member in
     * the output shape.
     */
    private static class SyncInputStream extends SyncOperationDocProvider {

        private SyncInputStream(IntermediateModel model, OperationModel opModel) {
            super(model, opModel);
        }

        @Override
        protected void applyReturns(DocumentationBuilder docBuilder) {
            docBuilder.returns(
                    "A {@link ResponseInputStream} containing data streamed from service. Note that this is an unmanaged " +
                    "reference to the underlying HTTP connection so great care must be taken to ensure all data if fully read " +
                    "from the input stream and that it is properly closed. Failure to do so may result in sub-optimal behavior " +
                    "and exhausting connections in the connection pool. The unmarshalled response object can be obtained via " +
                    "{@link ResponseInputStream#response()}. " + getStreamingOutputDocs());
            // Link to non-simple method for discoverability
            docBuilder.see("#getObject(%s, StreamingResponseHandler)", opModel.getMethodName(),
                           opModel.getInput().getVariableType());
        }

        @Override
        protected void applyParams(DocumentationBuilder docBuilder) {
            emitRequestParm(docBuilder);
        }
    }

    /**
     * Provider for simple method that takes no arguments and creates an empty request object.
     */
    private static class SyncNoArg extends SyncOperationDocProvider {

        private SyncNoArg(IntermediateModel model, OperationModel opModel) {
            super(model, opModel);
        }

        @Override
        protected void applyParams(DocumentationBuilder docBuilder) {
            // Link to non-simple method for discoverability
            docBuilder.see("#%s(%s)", opModel.getMethodName(), opModel.getInput().getVariableType());
        }
    }
}
