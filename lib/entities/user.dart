
class User{

  late String picture;
  late String thumbnail;
  late String gender;
  late String firstName;
  late String lastName;

  User();

  User.fromJson(Map<String, dynamic> data) {
    picture = data['picture'];
    thumbnail = data['thumbnail'];
    gender = data['gender'];
    firstName = data['firstName'];
    lastName = data['lastName'];
  }

  Map<String, dynamic> toJson() =>
      {
        'picture': picture,
        'thumbnail': thumbnail,
        'gender': gender,
        'firstName': firstName,
        'lastName': lastName,
      };

}
