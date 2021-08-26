package com.example.demo.payload;

public class UserIdCount {
	 private Long userId;
	    private Long count;
	    
		public UserIdCount(Long userId, Long count) {
			super();
			this.userId = userId;
			this.count = count;
		}
		public Long getUserId() {
			return userId;
		}
		public void setUserId(Long userId) {
			this.userId = userId;
		}
		public Long getCount() {
			return count;
		}
		public void setCount(Long count) {
			this.count = count;
		}
	    
}
