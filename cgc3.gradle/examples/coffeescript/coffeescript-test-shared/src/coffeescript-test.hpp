
const char failure_message[] = "failure";

#define ASSERT_EQUAL(expected, actual) \
do { \
	if (expected != actual) { \
		std::cerr << failure_message << " @ " << __LINE__ << \
		"\n\texpected " << expected << \
		"\n\tactual   " << actual << std::endl; \
		exit(EXIT_FAILURE); \
	} \
} while(false) \


#define ASSERT_SEQUAL(expected, actual) \
do { \
	if (expected != actual) { \
		std::cerr << failure_message << " @ " << __LINE__ << \
		"\n\texpected -> expected.txt" << \
		"\n\tactual   -> actual.txt" << std::endl; \
		std::ofstream("expected.txt") << expected << std::endl; \
		std::ofstream("actual.txt") << actual << std::endl; \
		exit(EXIT_FAILURE); \
	} \
} while(false) \



#define REQUIRE(CONDITION) \
do { \
	if (!(CONDITION)) { \
		std::cerr << failure_message << " @ " << __LINE__ << \
		("\n\t" #CONDITION) << std::endl; \
		exit(EXIT_FAILURE); \
	} \
} while(false) \

#define FAILURE(MESSAGE) \
do { \
		std::cerr << MESSAGE << " @ " << __LINE__ << std::endl; \
		exit(EXIT_FAILURE); \
} while(false) \

#include "tinfl.h"

std::string fstring(const std::string& fname, size_t& size)
{
	auto file = fopen(fname.c_str(), "rb");
	
	{
		auto failure_message = "failed to open the file";
		REQUIRE(nullptr != file);
	}

	fseek(file, 0, SEEK_END);
	size = ftell(file);
	fseek(file, 0, SEEK_SET);

	{
		auto failure_message = "opened file was the wrong size";
		ASSERT_EQUAL(397121,  size);
	}

	std::vector<char> data(size);
	REQUIRE(1 == fread(data.data(), size,1,file));
	fclose(file);
	auto result = std::string(data.data(), size);

	
	ASSERT_EQUAL(size,result.size());
	ASSERT_EQUAL(size,strlen(result.c_str()));

	return result;
}
