import {updatePagination} from "./pagination.js";

let currentPageNumber = localStorage.getItem("currentPageNumber") !== null ? parseInt(localStorage.getItem("currentPageNumber")) : 0;

async function fetchThreads(pageNumber) {
    console.log(`fetching threads at page: ${pageNumber}`);
    const response = await fetch(`/api/threads/all?page=${pageNumber}`);
    return await response.json();
}

export async function loadThreads(pageNumber) {
    const threads = await fetchThreads(pageNumber);
    const threadsContainer = document.getElementById("threadsContainer");
    threadsContainer.innerHTML = "";
    threads.content.forEach((thread) => {
        const threadElement = document.createElement("div");
        threadElement.innerHTML = `
        <hr>
        <div id="${thread.id}" class="thread threadContainer">
            <div class="threadContent">
                <div class="fileContainer">
                    ${renderFile(thread)}
                </div>
                <div class="threadInfo">
                    <span class="subject">${thread.subject}</span>
                    <span class="date">(${thread.dateTime})</span>
                    <span>#${thread.id}</span>
                    <span><button onclick="deleteThread(${thread.id})">X</button></span>
                </div>
                <blockquote class="threadBody">${thread.content}</blockquote>
            </div>
        </div>
        `;
        threadsContainer.appendChild(threadElement);
        document.getElementById(`password_${thread.id}`).addEventListener("keypress", (e) => {
            if(e.key === "Enter" && e.target.value !== "") {
                submitThreadPassword(thread.id);
            }
        });
    });
    let threadsLoaded = threads.content.length;
    updatePagination(currentPageNumber, threadsLoaded);
}

// function toggleFilePreview(imgId) {
//     const img = document.getElementById(`file_${imgId}`);
//     img.classList.toggle("fileThumb");
// }

window.toggleFilePreview = function(imgId) {
    const img = document.getElementById(`file_${imgId}`);
    img.classList.toggle("fileThumb");
}
function renderFile(thread) {
    const contentType = thread.fileContentType;
    const fileType = contentType.split("/")[1];
    // const fileLink = `<a href="data:${contentType};base64,${thread.fileData}" download="file_${thread.id}.${fileType}" class="fileLink">Download File</a>`;
    const fileLink = `<a href="data:${contentType};${thread.fileData}" download="file_${thread.id}.${fileType}" class="fileLink">Download File</a>`;

    const threadPasswordInput = `<input type="text" id="password_${thread.id}" class="threadPassword" placeholder="Thread Password">`;
    if (contentType.startsWith('image')) {
        return `${fileLink}
                <img src="data:${contentType};base64,${thread.fileData}" alt="file" id="file_${thread.id}" onclick="toggleFilePreview(${thread.id})" class="fileThumb">
                ${threadPasswordInput}`;
    } else if (contentType.startsWith('video')) {
        return `${fileLink}
                <video controls src="data:${contentType};base64,${thread.fileData}" id="file_${thread.id}" class="fileThumb">
                    Your browser does not support the video tag.
                </video>
               ${threadPasswordInput}`;
    } else if(contentType.startsWith('audio')) {
        return `${fileLink}
                <audio controls src="data:${contentType};base64,${thread.fileData}" id="file_${thread.id}" class="fileThumb">
                    Your browser does not support the audio tag.
                </audio>
                ${threadPasswordInput}`;
    } else {
        return `${fileLink}
                <img src="/image/file.png" alt="file" id="file_${thread.id}" class="fileThumb">
                ${threadPasswordInput}`;
    }
}

window.deleteThread = async function (threadId) {
    const password = document.getElementById(`password_${threadId}`).value;
    const response = await fetch(`/api/threads/${threadId}/${password}`, {
        method: "DELETE"
    });
    if (response.ok) {
        loadThreads(currentPageNumber);
        alert(`Thread ${threadId} deleted`);
    } else {
        alert(`Thread ${threadId} could not be deleted`);
    }

}

window.submitThreadPassword = async function (threadId) {
    const password = document.getElementById(`password_${threadId}`).value;
    if (password === null) return;
    const response = await fetch(`/api/threads/${threadId}/${password}`, {
        method: "POST"
    });
    if (response.ok) {
        alert(`Correct password for thread ${threadId}`);
    } else {
        alert(`Invalid password for thread ${threadId}`);
    }
}

// function downloadFile(threadId, fileData) {
//     const blob = base64ToBlob(fileData);
//     const url = URL.createObjectURL(blob);
//     const a = document.createElement('a');
//     a.href = url;
//     a.download = `file_${threadId}`;
//     document.body.appendChild(a);
//     a.click();
//     document.body.removeChild(a);
//     URL.revokeObjectURL(url);
// }
//
// function base64ToBlob(base64string) {
//     var binary_string = window.atob(base64string);
//     var len = binary_string.length;
//     var bytes = new Uint8Array( len );
//     for (var i = 0; i < len; i++) { bytes[i] = binary_string.charCodeAt(i); }
//     return new Blob([bytes.buffer], {type: "octet/stream"});
// }

window.onload = () => {
    (async () => {
        await loadThreads(currentPageNumber);
    })();
}
