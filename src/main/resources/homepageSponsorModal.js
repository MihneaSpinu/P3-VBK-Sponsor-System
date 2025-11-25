let responseMessage = document.getElementById("responseMessage_id");

let saveButton = document.getElementById("saveButton_id");
saveButton.addEventListener("Click", () => {

    let id = parseInt(); // ikke vist
    let name = document.getElementById("SponsorName").value;
    let cvr = document.getElementById("SponsorCVR").value;
    let contactPerson = document.getElementById("SponsorContactPerson").value;
    let email = document.getElementById("SponsorEmail").value;
    let phoneNumber = document.getElementById("SponsorPhoneNumber").value;
    let status = document.getElementById("SponsorStatus").value;
    let comment = document.getElementById("SponsorComment").value;

    fetch("/update/sponsor", {
        method: "POST",
        "Content-Type": "application/json",
        body: JSON.stringify({id: id,
                              sponsorName: name,
                              contactPerson: contactPerson,
                              cvrNumber: cvr,
                              email: email,
                              phoneNumber: phoneNumber,
                              status: status,
                              comments: comment
                              })
    })
    .then(response => responseMessage.innerText = `Successfully updated ${response} fields`)
    .catch(error => responseMessage.innerText = error);
});




/*
private String sponsorName;
private String contactPerson;
private String email;
private String phoneNumber;
private String cvrNumber;
private boolean status;
private String comments;
*/