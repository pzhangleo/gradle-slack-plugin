package com.mindera.gradle.slack.model

import net.gpedro.integrations.slack.SlackAttachment
import net.gpedro.integrations.slack.SlackField
import net.gpedro.integrations.slack.SlackMessage
import org.gradle.api.Task
import org.gradle.api.tasks.TaskState
import com.mindera.gradle.slack.utils.GitUtils

/**
 * Created by joaoprudencio on 09/05/15.
 */
class SlackMessageTransformer {
    private static final String TITLE_DEFAULT = 'Gradle build finished'
    private static final String COLOR_PASSED = 'good'
    private static final String COLOR_FAILED = 'danger'
    private static final String TASK_TITLE = 'Task'
    private static final String TASK_RESULT_TITLE = 'Task Result'
    private static final String TASK_RESULT_PASSED = 'Passed'
    private static final String TASK_RESULT_FAILED = 'Failed'
    private static final String BRANCH_TITLE = 'Git Branch'
    private static final String AUTHOR_TITLE = 'Git Author'
    private static final String COMMIT_TITLE = 'Git Commit'
    private static final String COMMIT_HASH  = 'Git Commit Hash'
    private static final String MESSAGE  = 'Message'
    private static final String QRCODE  = 'Qrcode'

    static SlackMessage buildSlackMessage(String title, Task task, TaskState state, String taskLog, String taskMessage) {
        Throwable failure = state.getFailure()
        boolean success = failure == null

        SlackMessage slackMessage = new SlackMessage(title ? title : TITLE_DEFAULT)

        SlackAttachment attachments = new SlackAttachment()
        attachments.setColor(success ? COLOR_PASSED : COLOR_FAILED)

        StringBuilder message = new StringBuilder()
        message.append(task.getDescription())
        if (!success && failure != null && failure.getCause() != null) {
            message.append('\n')
            message.append(failure.getCause())
        }
        if (!success && taskLog != null) {
            message.append('\n')
            message.append(taskLog)
        }
        attachments.setText(message.toString())
        attachments.setFallback(message.toString())

        SlackField taskField = new SlackField()
        taskField.setTitle(TASK_TITLE)
        taskField.setValue(task.getName())
        taskField.setShorten(true)
        attachments.addFields(taskField)

        SlackField resultField = new SlackField()
        resultField.setTitle(TASK_RESULT_TITLE)
        resultField.setValue(success ? TASK_RESULT_PASSED : TASK_RESULT_FAILED)
        resultField.setShorten(true)
        attachments.addFields(resultField)

        SlackField branchField = new SlackField()
        branchField.setTitle(BRANCH_TITLE)
        branchField.setValue(GitUtils.branchName())
        branchField.setShorten(true)
        attachments.addFields(branchField)

        SlackField authorField = new SlackField()
        authorField.setTitle(AUTHOR_TITLE)
        authorField.setValue(GitUtils.lastCommitAuthor())
        authorField.setShorten(true)
        attachments.addFields(authorField)

        SlackField commitField = new SlackField()
        commitField.setTitle(COMMIT_TITLE)
        commitField.setValue(GitUtils.lastCommitMessage())
        commitField.setShorten(false)
        attachments.addFields(commitField)

        SlackField hashField = new SlackField()
        hashField.setTitle(COMMIT_HASH)
        hashField.setValue(GitUtils.lastCommitHash())
        hashField.setShorten(false)
        attachments.addFields(hashField)

        if (taskMessage != null && success) {
            SlackField messageField = new SlackField()
            messageField.setTitle(MESSAGE)
            messageField.setValue(taskMessage)
            messageField.setShorten(false)
            attachments.addFields(messageField)

//            SlackField codeField = new SlackField()
//            codeField.setTitle(QRCODE)
//            codeField.addAllowedMarkdown("fields")
//            codeField.setValue(String.format("![QRCODE](%s)", "http://pan.baidu.com/share/qrcode?w=150&h=150&url="+taskMessage))
//            codeField.setShorten(true)
//            attachments.addFields(codeField)

            attachments.setImageUrl("http://pan.baidu.com/share/qrcode?w=150&h=150&url="+taskMessage)
        }

        slackMessage.addAttachments(attachments)

        return slackMessage
    }
}
