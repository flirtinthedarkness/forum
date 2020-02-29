$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// get post content and title
	var title = $("#new-post-title").val();
	var content = $("#new-post-content").val();

	$.post(
		CONTEXT_PATH + "/discuss/addPost",
		{
			"title":title,
			"content":content
		},
		function (data) {
			data = $.parseJSON(data);
			$("#hintBody").text(data.msg);
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				if (data.code == 200) {
					window.location.reload();
				}
			}, 2000);
		}
	)
}