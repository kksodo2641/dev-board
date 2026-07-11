document.addEventListener("DOMContentLoaded", () => {
    // 댓글 목록 로딩
    loadComments();

    // 대댓글 작성/취소 이벤트
    document.addEventListener("click", event => {
        if (event.target.classList.contains("comment-action-btn")) {
            toggleReplyForm(event.target);
            return;
        }

        if (event.target.classList.contains("cancel-reply-btn")) {
            closeReplyForm(event.target);
        }
    });

    // 댓글 작성 이벤트
    document.addEventListener("submit", event => {
        if (event.target.id === "comment-form") {
            submitComment(event);

        } else if (event.target.classList.contains("reply-form")) {
            submitReply(event);
        }
    });
});

async function loadComments() {
    const commentCard = document.querySelector(".comment-card");
    const commentList = document.querySelector("#comment-list");
    const commentCount = document.querySelector("#comment-count");

    const boardId = commentCard.dataset.boardId;

    try {
        const response = await fetch(`/boards/${boardId}/comments`);

        if (!response.ok) {
            throw new Error("댓글 목록 조회에 실패했습니다.");
        }

        const comments = await response.json();

        commentCount.textContent = comments.length;
        renderComments(comments);

    } catch (error) {
        commentList.replaceChildren();

        const errorMessage = document.createElement("div");
        errorMessage.classList.add("comment-empty");
        errorMessage.textContent = "댓글을 불러오지 못했습니다.";

        commentList.appendChild(errorMessage);

        console.error(error);
    }
}

async function submitComment(event) {
    event.preventDefault(); // 브라우저의 form submit 기본 동작 차단

    const commentForm = event.target;
    const textarea = commentForm.querySelector("#comment-content");
    const error = commentForm.querySelector("#comment-error");

    const content = textarea.value.trim();

    if (content.length === 0) {
        error.textContent = "댓글 내용을 입력해주세요.";
        textarea.focus(); // textarea로 사용자 커서 포커싱
        return;
    }

    try {
        await writeComment(content, null);

        textarea.value = "";
        error.textContent = "";

        await loadComments();

    } catch (exception) {
        console.error(exception);
        error.textContent = exception.message;
    }
}

async function submitReply(event) {
    event.preventDefault();

    const replyForm = event.target;
    const textarea = replyForm.querySelector("textarea[name='content']");
    const parentInput = replyForm.querySelector("input[name='parentId']");
    const error = replyForm.querySelector(".reply-error");

    const content = textarea.value.trim();
    const parentId = Number(parentInput.value);

    if (content.length === 0) {
        error.textContent = "답글 내용을 입력해주세요.";
        textarea.focus();
        return;
    }

    try {
        await writeComment(content, parentId);

        textarea.value = "";
        error.textContent = "";

        await loadComments();

    } catch (exception) {
        console.error(exception);
        error.textContent = exception.message;
    }
}

async function writeComment(content, parentId) {
    const commentCard = document.querySelector(".comment-card");
    const boardId = commentCard.dataset.boardId;

    const response = await fetch(`/boards/${boardId}/comments`, {
        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify({
            content: content,
            parentId: parentId
        })
    });

    if (!response.ok) {
        const errorResponse = await response.json();
        throw new Error(errorResponse.message || "댓글 작성에 실패했습니다.");
    }
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

    if (comment.hasParent) {
        article.classList.add("comment-reply");
    }

    article.appendChild(createCommentMeta(comment));
    article.appendChild(createCommentContent(comment));

    const commentCard = document.querySelector(".comment-card");
    const isLogin = commentCard.dataset.login === "true";

    if (isLogin && !comment.hasParent) {
        article.appendChild(createCommentFooter(comment));
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

function createCommentFooter(comment) {
    const footer = document.createElement("footer");
    footer.classList.add("comment-footer");

    const actions = document.createElement("div");
    actions.classList.add("comment-actions");

    const replyButton = document.createElement("button");
    replyButton.type = "button";
    replyButton.classList.add("comment-action-btn");
    replyButton.dataset.commentId = comment.commentId;
    replyButton.textContent = "↳ 답글 달기";

    actions.appendChild(replyButton);
    footer.appendChild(actions);

    return footer;
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

    const error = document.createElement("div");
    error.classList.add("error", "reply-error");

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
    form.appendChild(error);
    form.appendChild(buttonGroup);

    container.appendChild(target);
    container.appendChild(form);

    return container;
}

function toggleReplyForm(button) {
    const commentItem = button.closest(".comment-item");
    const currentForm = commentItem.querySelector(".reply-form-container");

    document
        .querySelectorAll(".reply-form-container")
        .forEach(form => {
            if (form !== currentForm) {
                form.style.display = "none";

                const textarea = form.querySelector("textarea");
                if (textarea !== null) {
                    textarea.value = "";
                }

                const error = form.querySelector(".reply-error");
                if (error !== null) {
                    error.textContent = "";
                }
            }
        });

    if (currentForm.style.display === "block") {
        currentForm.style.display = "none";

        currentForm.querySelector("textarea")
                   .value = "";

        const error = currentForm.querySelector(".reply-error");
        if (error !== null) {
            error.textContent = "";
        }

        return;
    }

    currentForm.style.display = "block";
    currentForm.querySelector("textarea").focus();
}

function closeReplyForm(button) {
    const formContainer = button.closest(".reply-form-container");

    formContainer.style.display = "none";
    formContainer.querySelector("textarea")
                 .value = "";

    const error = formContainer.querySelector(".reply-error");
    if (error !== null) {
        error.textContent = "";
    }
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
