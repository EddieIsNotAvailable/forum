import {updatePagination} from "./pagination.js";

let currentPageNumber = localStorage.getItem("currentPageNumber") !== null ? parseInt(localStorage.getItem("currentPageNumber")) : 0;

async function fetchThreadFile(threadId) {
    const response = await fetch(`/api/threads/${threadId}/file`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/octet-stream'
        }
    });
    if (!response.ok) {
        throw new Error('Failed to retrieve file');
    }
    // return response.blob();
    const blob = await response.blob();
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(blob);
    });
}

async function fetchThreads(pageNumber) {
    const response = await fetch(`/api/threads/all?page=${pageNumber}`);
    if (!response.ok) {
        throw new Error(`Failed to fetch threads: ${response.status}`);
    }
    return response.json();
}

export async function loadThreads(pageNumber) {
    const threads = await fetchThreads(pageNumber);
    const threadsContainer = document.getElementById("threadsContainer");
    threadsContainer.innerHTML = "";
    for(const thread of threads.content) {
        const renderedFile = await renderFile(thread);
        const threadElement = document.createElement("div");
        threadElement.innerHTML = `
        <hr>
        <div id="${thread.id}" class="thread threadContainer">
            <div class="threadContent">
                <div class="fileContainer">
                    ${renderedFile}
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
    }
    threads.content.forEach((thread) => {
        document.getElementById(`password_${thread.id}`).addEventListener("keypress", (e) => {
            if(e.key === "Enter" && e.target.value !== "") {
                submitThreadPassword(thread.id);
            }
        });
    });

    let threadsLoaded = threads.content.length;
    updatePagination(currentPageNumber, threadsLoaded);
}

window.toggleFilePreview = function(imgId) {
    const img = document.getElementById(`file_${imgId}`);
    img.classList.toggle("fileThumb");
}

async function renderFile(thread) {
    const contentType = thread.fileContentType;
    const fileType = contentType.split("/")[1];
    const fileData = await fetchThreadFile(thread.id);
    const fileLink = `<a href="data:${contentType};${fileData}" download="file_${thread.id}.${fileType}" class="fileLink">Download File</a>`;

    const threadPasswordInput = `<input type="text" id="password_${thread.id}" class="threadPassword" placeholder="Thread Password">`;
    if (contentType.startsWith('image')) {
        return `${fileLink}
                <img src="data:${contentType};${fileData}" alt="file" id="file_${thread.id}" onclick="toggleFilePreview(${thread.id})" class="fileThumb">
                ${threadPasswordInput}`;
    } else if (contentType.startsWith('video')) {
        return `${fileLink}
                <video controls src="data:${contentType};${fileData}" id="file_${thread.id}" class="fileThumb">
                    Your browser does not support the video tag.
                </video>
               ${threadPasswordInput}`;
    } else if(contentType.startsWith('audio')) {
        return `${fileLink}
                <audio controls src="data:${contentType};${fileData}" id="file_${thread.id}" class="fileThumb">
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

window.onload = () => {
    (async () => {
        await loadThreads(currentPageNumber);
    })();
}
