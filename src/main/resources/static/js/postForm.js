const form = document.getElementById("threadForm");
const notification = document.getElementById("postFormNotification");


function formNotif(message, color) {
        notification.style.display = "block";
        notification.style.color = color;
        notification.textContent = message;
}

form.addEventListener("submit", (e) => {
    e.preventDefault();

    fetch("/api/threads/create", {
        method: "POST",
        body: new FormData(form)
    }).then((response) => {
        if (response.ok) {
            location.reload();
            // formNotif("Post created successfully!", "green")
            // setTimeout(() => {
            //     location.reload();
            // }, 3000);
        } else response.text().then((msg) => { throw new Error().cause = msg; });
    }).catch((e) => {
        const msg = e.cause || "No response from server";
        formNotif(msg, "red");
        setTimeout(() => {
            notification.style.display = "none";
            notification.textContent = "";
        }, 5000);
    });
});

const postForm = document.getElementsByClassName("postForm")[0];
const toggleFormButton = document.getElementById("toggleFormButton");
toggleFormButton.addEventListener("click", () => {
    if (postForm.style.display === "none") {
        toggleFormButton.textContent = "Close";
        postForm.style.display = "block";
    } else {
        toggleFormButton.textContent = "Make a Post";
        postForm.style.display = "none";
    }
});