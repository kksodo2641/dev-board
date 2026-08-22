document.addEventListener("DOMContentLoaded", () => {
    // 댓글 목록 로딩
    loadComments();

    document.addEventListener("click", event => {
        const target = event.target;
        const classList = target.classList;

        if (classList.contains("reply-action-btn")) {
            toggleReplyForm(target);

        } else if (classList.contains("cancel-reply-btn")) {
            closeReplyForm(target);

        } else if (classList.contains("edit-comment-btn")) {
            openEditForm(target);

        } else if (classList.contains("cancel-edit-btn")) {
            closeEditForm(target);

        } else if (classList.contains("delete-comment-btn")) {
            handleDeleteComment(target);
        }
    });

    document.addEventListener("submit", event => {
        const target = event.target;
        const classList = target.classList;

        if (target.id === "comment-form") {
            submitComment(event);

        } else if (classList.contains("reply-form")) {
            submitReply(event);

        } else if (classList.contains("comment-edit-form")) {
            submitEditComment(event);
        }
    });
});

async function loadComments() {
    const commentCard = document.querySelector(".comment-card");
    const commentList = document.querySelector("#comment-list");
    const commentCount = document.querySelector("#comment-count");

    const boardId = commentCard.dataset.boardId;

    const url = `/boards/${boardId}/comments`;
    const fallbackMessage = "댓글을 불러오지 못했습니다.";

    try {
        const response = await fetchApi(url, fallbackMessage);
        const success = await handleApiResponse(response, fallbackMessage);

        if (!success) {
            return;
        }

        const comments = await response.json();

        commentCount.textContent = comments.length;
        renderComments(comments);

    } catch (error) {
        commentList.replaceChildren();

        const errorElement = document.createElement("div");
        errorElement.classList.add("comment-empty");
        errorElement.textContent = fallbackMessage;

        commentList.appendChild(errorElement);
        console.error(error);
    }
}

async function submitComment(event) {
    event.preventDefault(); // 브라우저의 form submit 기본 동작 차단

    const commentForm = event.target;
    const textarea = commentForm.querySelector("#comment-content");
    const errorElement = commentForm.querySelector("#comment-error");

    const content = textarea.value.trim();

    if (content.length === 0) {
        errorElement.textContent = "댓글 내용을 입력해주세요.";
        textarea.focus(); // textarea로 사용자 커서 포커싱
        return;
    }

    try {
        const success = await writeComment(content, null);
        if (!success) {
            return;
        }

        textarea.value = "";
        errorElement.textContent = "";

        await loadComments();

    } catch (error) {
        console.error(error);
        errorElement.textContent = error.message;
    }
}

async function submitReply(event) {
    event.preventDefault();

    const replyForm = event.target;
    const textarea = replyForm.querySelector("textarea[name='content']");
    const parentInput = replyForm.querySelector("input[name='parentId']");
    const errorElement = replyForm.querySelector(".reply-error");

    const content = textarea.value.trim();
    const parentId = Number(parentInput.value);

    if (content.length === 0) {
        errorElement.textContent = "답글 내용을 입력해주세요.";
        textarea.focus();
        return;
    }

    try {
        const success = await writeComment(content, parentId);
        if (!success) {
            return;
        }

        textarea.value = "";
        errorElement.textContent = "";

        await loadComments();

    } catch (error) {
        console.error(error);
        errorElement.textContent = error.message;
    }
}

async function submitEditComment(event) {
    event.preventDefault();

    const editForm = event.target;
    const textarea = editForm.querySelector(".comment-edit-textarea");
    const errorElement = editForm.querySelector(".edit-error");

    const commentId = Number(editForm.dataset.commentId);
    const content = textarea.value.trim();

    if (content.length === 0) {
        errorElement.textContent = "댓글 내용을 입력해주세요.";
        textarea.focus();
        return;
    }

    try {
        const success = await updateComment(commentId, content);
        if (success) {
            await loadComments();
        }

    } catch (error) {
        console.error(error);
        errorElement.textContent = error.message;
    }
}

async function handleDeleteComment(button) {
    if (!confirm("댓글을 삭제하시겠습니까?")) {
        return;
    }

    const commentId = Number(button.dataset.commentId);

    try {
        const success = await deleteComment(commentId);
        if (!success) {
            return;
        }

        await loadComments();

    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

async function writeComment(content, parentId) {
    const commentCard = document.querySelector(".comment-card");
    const boardId = commentCard.dataset.boardId;

    const url = `/boards/${boardId}/comments`;

    const options = {
        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify({
            content: content,
            parentId: parentId
        })
    };

    const fallbackMessage = "댓글 작성에 실패했습니다.";

    const response = await fetchApi(url, fallbackMessage, options);
    return handleApiResponse(response, fallbackMessage);
}

async function updateComment(commentId, content) {
    const url = `/comments/${commentId}`;

    const options = {
        method: "PATCH",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify({
            content: content
        })
    };

    const fallbackMessage = "댓글 수정에 실패했습니다.";

    const response = await fetchApi(url, fallbackMessage, options);
    return handleApiResponse(response, fallbackMessage);
}

async function deleteComment(commentId) {
    const url = `/comments/${commentId}`;

    const options = {
        method: "DELETE"
    };

    const fallbackMessage = "댓글 삭제에 실패했습니다.";

    const response = await fetchApi(url, fallbackMessage, options);
    return handleApiResponse(response, fallbackMessage);
}

async function fetchApi(url,
                        fallbackMessage,
                        options = {}) {
    try {
        return await fetch(url, options);

    } catch {
        throw new Error(fallbackMessage);
    }
}

async function handleApiResponse(response, fallbackMessage) {
    if (response.ok) {
        return true;
    }

    let errorResponse;

    try {
        errorResponse = await response.json();
    } catch {
        throw new Error(fallbackMessage);
    }

    if (response.status === 401
            && errorResponse?.code === "LOGIN_REQUIRED") {
        redirectToLogin();
        return false;
    }

    throw new Error(errorResponse?.message || fallbackMessage);
}

function redirectToLogin() {
    alert("로그인 정보가 만료되었습니다. 다시 로그인해주세요.");

    const redirectURL = window.location.pathname  // 현재 주소 (예: "/boards/15")
                        + window.location.search; // 쿼리 스트링 (예: "?page=2")

    // 로그인 페이지로 이동
    window.location.href = `/members/login?redirectURL=${encodeURIComponent(redirectURL)}`;
}

function renderComments(comments) {
    const commentList = document.querySelector("#comment-list");
    commentList.replaceChildren();

    if (comments.length === 0) {
        const emptyMessage = document.createElement("div");
        emptyMessage.classList.add("comment-empty");
        emptyMessage.textContent = "아직 작성된 댓글이 없습니다.";

        const lineBreak = document.createElement("br");

        const subMessage = document.createTextNode("첫 번째 댓글을 작성해보세요.");

        emptyMessage.appendChild(lineBreak);
        emptyMessage.appendChild(subMessage);

        commentList.appendChild(emptyMessage);
        return;
    }

    const fragment = document.createDocumentFragment();

    comments.forEach(comment => {
        fragment.appendChild(createCommentElement(comment));
    });

    commentList.appendChild(fragment);
}

function createCommentElement(comment) {
    const article = document.createElement("article");
    article.classList.add("comment-item");

    const isReply = comment.hasParent;

    if (isReply) {
        article.classList.add("comment-reply");
    }

    article.appendChild(createCommentMeta(comment));
    article.appendChild(createCommentContent(comment));

    const commentCard = document.querySelector(".comment-card");
    const isLogin = commentCard.dataset.login === "true";

    const canReply = isLogin && !isReply;
    const hasAction = canReply || comment.canEdit || comment.canDelete;

    if (hasAction) {
        article.appendChild(createCommentFooter(comment, canReply));
    }

    if (comment.canEdit) {
        article.appendChild(createEditForm(comment));
    }

    if (canReply) {
        article.appendChild(createReplyForm(comment));
    }

    return article;
}

function createCommentMeta(comment) {
    const header = document.createElement("header");
    header.classList.add("comment-meta");

    const writer = document.createElement("span");
    writer.classList.add("comment-writer");
    writer.textContent = comment.writerNickname;

    const divider = document.createElement("span");
    divider.classList.add("comment-divider");
    divider.textContent = "·";

    const time = document.createElement("time");
    time.classList.add("comment-date");
    time.textContent = formatDateTime(comment.createdAt);

    header.appendChild(writer);
    header.appendChild(divider);
    header.appendChild(time);

    return header;
}

function createCommentContent(comment) {
    const content = document.createElement("div");
    content.classList.add("comment-content");

    if (comment.deleted) {
        content.classList.add("deleted-comment");
    }

    content.textContent = comment.content;

    return content;
}

function createCommentFooter(comment, canReply) {
    const footer = document.createElement("footer");
    footer.classList.add("comment-footer");

    const actions = document.createElement("div");
    actions.classList.add("comment-actions");

    if (canReply) {
        const replyButton = document.createElement("button");
        replyButton.type = "button";
        replyButton.classList.add(
            "comment-action-btn",
            "reply-action-btn"
        );
        replyButton.dataset.commentId = comment.commentId;
        replyButton.textContent = "↳ 답글 달기";

        actions.appendChild(replyButton);
    }

    if (comment.canEdit) {
        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.classList.add(
            "comment-action-btn",
            "edit-comment-btn"
        );
        editButton.dataset.commentId = comment.commentId;
        editButton.textContent = "수정";

        actions.appendChild(editButton);
    }

    if (comment.canDelete) {
        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.classList.add(
            "comment-action-btn",
            "delete-comment-btn"
        );
        deleteButton.dataset.commentId = comment.commentId;
        deleteButton.textContent = "삭제";

        actions.appendChild(deleteButton);
    }

    footer.appendChild(actions);

    return footer;
}

function createEditForm(comment) {
    const form = document.createElement("form");

    form.classList.add("comment-edit-form");
    form.dataset.commentId = comment.commentId;

    const textarea = document.createElement("textarea");
    textarea.name = "content";
    textarea.rows = 4;
    textarea.maxLength = 2000;
    textarea.classList.add("comment-edit-textarea");
    textarea.value = comment.content;
    textarea.dataset.originalContent = comment.content;

    const errorElement = document.createElement("div");
    errorElement.classList.add(
        "error",
        "edit-error"
    );

    const buttonGroup = document.createElement("div");
    buttonGroup.classList.add("comment-edit-button-group");

    const submitButton = document.createElement("button");
    submitButton.type = "submit";
    submitButton.classList.add(
        "btn",
        "btn-primary"
    );
    submitButton.textContent = "수정 완료";

    const cancelButton = document.createElement("button");
    cancelButton.type = "button";
    cancelButton.classList.add(
        "btn",
        "btn-secondary",
        "cancel-edit-btn"
    );
    cancelButton.textContent = "취소";

    buttonGroup.appendChild(submitButton);
    buttonGroup.appendChild(cancelButton);

    form.appendChild(textarea);
    form.appendChild(errorElement);
    form.appendChild(buttonGroup);

    return form;
}

function createReplyForm(comment) {
    /*
        <div class="reply-form-container">
            <div class="reply-target">
                ↳ xxx님에게 답글 작성
            </div>

            <form class="reply-form">
                <input type="hidden" name="parentId" value="부모 댓글 ID">

                <textarea name="content" class="reply-textarea"
                          rows="3" placeholder="답글을 입력해주세요."></textarea>

                <div class="error reply-error"></div>

                <div class="reply-button-group">
                    <button type="submit" class="btn btn-primary">답글 작성</button>
                    <button type="button" class="btn btn-secondary cancel-reply-btn">취소</button>
                </div>
            </form>
        </div>
    */
    const container = document.createElement("div");
    container.classList.add("reply-form-container");

    const target = document.createElement("div");
    target.classList.add("reply-target");
    target.textContent = `↳ ${comment.writerNickname}님에게 답글 작성`;

    const form = document.createElement("form");
    form.classList.add("reply-form");

    const parentInput = document.createElement("input");
    parentInput.type = "hidden";
    parentInput.name = "parentId";
    parentInput.value = comment.commentId;

    const textarea = document.createElement("textarea");
    textarea.name = "content";
    textarea.rows = 3;
    textarea.classList.add("reply-textarea");
    textarea.placeholder = "답글을 입력해주세요.";

    const errorElement = document.createElement("div");
    errorElement.classList.add("error", "reply-error");

    const buttonGroup = document.createElement("div");
    buttonGroup.classList.add("reply-button-group");

    const submitButton = document.createElement("button");
    submitButton.type = "submit";
    submitButton.classList.add("btn", "btn-primary");
    submitButton.textContent = "답글 작성";

    const cancelButton = document.createElement("button");
    cancelButton.type = "button";
    cancelButton.classList.add("btn", "btn-secondary", "cancel-reply-btn");
    cancelButton.textContent = "취소";

    buttonGroup.appendChild(submitButton);
    buttonGroup.appendChild(cancelButton);

    form.appendChild(parentInput);
    form.appendChild(textarea);
    form.appendChild(errorElement);
    form.appendChild(buttonGroup);

    container.appendChild(target);
    container.appendChild(form);

    return container;
}

function toggleReplyForm(button) {
    const commentItem = button.closest(".comment-item");
    const currentForm = commentItem.querySelector(".reply-form-container");

    // 열려 있는 모든 댓글 수정 폼 닫기
    document.querySelectorAll(".comment-edit-form")
            .forEach(form => hideEditForm(form));

    // 현재 답글 폼을 제외한 다른 답글 폼 닫기
    document.querySelectorAll(".reply-form-container")
            .forEach(form => {
                if (form !== currentForm) {
                    hideReplyForm(form);
                }
            });

    if (currentForm.style.display === "block") {
        hideReplyForm(currentForm);
        return;
    }

    currentForm.style.display = "block";
    currentForm.querySelector("textarea").focus();
}

function closeReplyForm(button) {
    const formContainer = button.closest(".reply-form-container");
    hideReplyForm(formContainer);
}

function hideReplyForm(formContainer) {
    const textarea = formContainer.querySelector("textarea");
    const errorElement = formContainer.querySelector(".reply-error");

    formContainer.style.display = "none";
    textarea.value = "";
    errorElement.textContent = "";
}

function openEditForm(button) {
    const commentItem = button.closest(".comment-item");
    const currentForm = commentItem.querySelector(".comment-edit-form");

    document.querySelectorAll(".reply-form-container")
            .forEach(form => hideReplyForm(form));

    document.querySelectorAll(".comment-edit-form")
            .forEach(form => {
                if (form !== currentForm) {
                    hideEditForm(form);
                }
            });

    const content = commentItem.querySelector(".comment-content");
    const footer = commentItem.querySelector(".comment-footer");

    content.style.display = "none";

    if (footer !== null) {
        footer.style.display = "none";
    }

    currentForm.style.display = "block";

    const textarea = currentForm.querySelector(".comment-edit-textarea");
    textarea.focus();
    textarea.setSelectionRange(
        textarea.value.length,
        textarea.value.length
    );
}

function closeEditForm(button) {
    const form = button.closest(".comment-edit-form");
    hideEditForm(form);
}

function hideEditForm(form) {
    const commentItem = form.closest(".comment-item");
    const content = commentItem.querySelector(".comment-content");
    const footer = commentItem.querySelector(".comment-footer");
    const textarea = form.querySelector(".comment-edit-textarea");
    const errorElement = form.querySelector(".edit-error");

    form.style.display = "none";
    content.style.display = "";

    if (footer !== null) {
        footer.style.display = "";
    }

    textarea.value = textarea.dataset.originalContent;
    errorElement.textContent = "";
}

function formatDateTime(createdAt) {
    const date = new Date(createdAt);

    return date.toLocaleString("ko-KR", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        hour12: false
    });
}
