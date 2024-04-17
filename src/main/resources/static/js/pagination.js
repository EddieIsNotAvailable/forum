import {loadThreads} from "./loadThreads.js";

export function updatePagination(currentPageNumber,threadsLoaded) {
    if(currentPageNumber >0 && threadsLoaded === 0) {
        currentPageNumber--;
        localStorage.setItem("currentPageNumber", String(currentPageNumber));
        loadThreads(currentPageNumber);
        return;
    }

    const paginationContainer = document.getElementById("paginationContainer");
    paginationContainer.innerHTML = "";
    if(currentPageNumber > 0) {
        const prevButton = document.createElement("button");
        prevButton.textContent = `Prev`;
        prevButton.addEventListener("click", () => {
            currentPageNumber--;
            localStorage.setItem("currentPageNumber", String(currentPageNumber));
            loadThreads(currentPageNumber);
        });
        paginationContainer.appendChild(prevButton);
    }

    if(threadsLoaded === 10) {
        const nextButton = document.createElement("button");
        nextButton.textContent = `Next`;
        nextButton.addEventListener("click", () => {
            currentPageNumber++;
            localStorage.setItem("currentPageNumber", String(currentPageNumber));
            loadThreads(currentPageNumber);
        });
        paginationContainer.appendChild(nextButton);
    }
}

