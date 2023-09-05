const uploadForm = document.getElementById('myformfileinput');
const form = document.getElementById('uploadForm')
const uploadURL = form.getAttribute("action");
const completedCount = 0; // added for loadend scenario
const fileModal = document.getElementById("file-modal");
const token = document.querySelector("meta[name='_csrf']").getAttribute('content')
const fileOptionsButton = document.getElementById("file-options");

//$('#_csrf').attr('content');
const header = document.querySelector("meta[name='_csrf_header']").getAttribute('content')
//$('#_csrf_header').attr('content');



  htmx.on('#uploadForm', 'htmx:xhr:progress', function(evt) {
        const progress_value = evt.detail.loaded/evt.detail.total * 100;
        if (isFinite(progress_value))
            htmx.find('#progress').setAttribute('value', evt.detail.loaded/evt.detail.total * 100);
        console.log("SETTING PROGRESS VALUE: " + evt.detail.loaded/evt.detail.total * 100)
  });




    htmx.on('#uploadForm', 'htmx:afterRequest', function(evt) {
        htmx.find('#progress').setAttribute('value', 0);
        console.log("Request finished");
    });


    htmx.on('#uploadForm', 'change', uploadFiles);

    htmx.on('#file-modal', 'click', e => {
          const dialogDimensions = fileModal.getBoundingClientRect()
          if (
            e.clientX < dialogDimensions.left ||
            e.clientX > dialogDimensions.right ||
            e.clientY < dialogDimensions.top ||
            e.clientY > dialogDimensions.bottom
          ) {
            fileModal.close();
          }
          console.log("FILE MODAL CLICKED");
    });


    htmx.on("#file-options", 'click', e => {
        fileModal.showModal();
    });

// Attach event handlers to file modifier buttons
//htmx.findAll("delete-file-button");
//const fileRename = htmx.findAll("");
//const file = htmx.findAll("");
//const fileDelete = htmx.find("");

//Array.from(htmx.findAll(".directory"))
//    .forEach(element => {
//        console.log("setting element onclick")
//        htmx.on(element, "click", event => {
//            history.pushState("", "", window.location.href+"/"+element.innerText)
//        });
//    });

function uploadFiles() {
    for (let i = 0; i < uploadForm.files.length; i++) {
        htmx.ajax('POST', uploadURL, {
        target:'#main-file-table-tbody',
        swap:'beforeend',
        values:{'file': uploadForm.files[i]},
        headers:{'X-CSRF-TOKEN': token},
        source: form // hacky way of solving this, https://github.com/bigskysoftware/htmx/issues/1560
        });
    }
}

function handleUploadUrl(event) {
    console.log("UPDATING SHIT:")
  const newUrl = window.location.pathname;
  if (newUrl.length < 1) {
    return;
  }
  const parts = newUrl.substr(1, newUrl.length).split("/");

  if (parts.length == 1) {
    return;
  }
  parts.splice(0, 1, "/upload");
  form.action = parts.join("/");
}

const updateUploadURL = (evt) => {
    const newUrl = evt.detail.path;
    if (newUrl.length < 1) {
        return;
    }
    const parts = newUrl.substr(1, newUrl.length).split("/");

    if (parts.length == 1) {
        return;
    }
    parts.splice(0, 1, "/upload"); // append /upload to current url
    form.action = parts.join("/");
    uploadURL = form.action;
}

const update = (event) => {
    updateUploadURL(event);
    addFileCheckBoxListeners();
};

const addFileCheckBoxListeners = () => {
    // get each file checkbox
    const checkboxes = htmx.findAll(".table-cell-checkbox");
    Array.from(checkboxes).forEach(checkbox => htmx.on(checkbox, ""))
    // add listener to add item into selected_file
}

document.body.addEventListener('htmx:pushedIntoHistory', update);
document.body.addEventListener('htmx:historyRestore', update);


// File selection TODO
// if download / delete / move etc
// is happening:
// 1. get each checkbox element
// 2. loop through each, check if it's checked
// 3. if yes, do the job in batch.
// 4. handle returns from the server, swap elements / delete them.
let selected_files = []
