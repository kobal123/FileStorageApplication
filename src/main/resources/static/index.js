const uploadForm = document.getElementById('myformfileinput');
const form = document.getElementById('uploadForm')
const uploadURL = form.getAttribute("action");
const completedCount = 0; // added for loadend scenario
const token = document.querySelector("meta[name='_csrf']").getAttribute('content')
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


    htmx.on('#uploadForm', 'change', function(evt) {
        console.log("FILES:");
        console.log(uploadForm.files);
        uploadFiles();
    });


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
