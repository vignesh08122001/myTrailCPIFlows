
import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonOutput

def Message processData(Message message) {

    // Read existing properties
    int pollCount = (message.getProperty("pollCount") ?: "0") as Integer
    String jobId = message.getProperty("jobId") as String

    // Increment polling attempt
    pollCount++

    // Simulate an external job status API
    String status

    if (pollCount < 3) {
        status = "RUNNING"
    } else {
        status = "COMPLETED"
    }

    // Update exchange properties
    message.setProperty("pollCount", pollCount)
    message.setProperty("status", status)

    // Prepare mock API response
    def response = [
        jobId    : jobId,
        pollCount: pollCount,
        status   : status,
        message  : status == "COMPLETED"
                   ? "Employee export completed successfully"
                   : "Employee export is still processing"
    ]

    String responseBody = JsonOutput.toJson(response)

    message.setBody(responseBody)

    // Add each polling response to the MPL
    def messageLog = messageLogFactory.getMessageLog(message)

    if (messageLog != null) {
        messageLog.addAttachmentAsString(
            "Polling Attempt ${pollCount}",
            responseBody,
            "application/json"
        )
    }

    return message
}
