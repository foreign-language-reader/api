resource "aws_s3_bucket" "foreign-language-reader-frontend" {
  bucket = "foreign-language-reader-frontend-${var.env}"
  acl    = "public-read"

  website {
    index_document = "index.html"
    error_document = "error.html"
  }
}

resource "aws_s3_bucket_policy" "public-access" {
  bucket = aws_s3_bucket.foreign-language-reader-frontend.id

  policy = <<POLICY
{
    "Version": "2008-10-17",
    "Id": "PolicyForPublicWebsiteContent",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": {
                "AWS": "*"
            },
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::foreign-language-reader-frontend-dev/*"
        }
    ]
}
POLICY
}
